package com.example.shortener.link;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final LinkRepository linkRepository;
    private final ClickEventRepository clickEventRepository;

    @Transactional(readOnly = true)
    public StatsResponse getStats(String code) {
        Link link = linkRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found"));

        long total = clickEventRepository.countByLinkId(link.getId());

        List<DailyClicks> perDay = clickEventRepository.countClicksPerDay(link.getId()).stream()
                .map(row -> new DailyClicks(
                        LocalDate.parse(row[0].toString()),
                        ((Number) row[1]).longValue()))
                .toList();

        List<ReferrerCount> topReferrers = clickEventRepository
                .countByReferrer(link.getId(), PageRequest.of(0, 5)).stream()
                .map(row -> new ReferrerCount(
                        row[0] == null ? "direct" : row[0].toString(),
                        ((Number) row[1]).longValue()))
                .toList();

        return new StatsResponse(link.getCode(), link.getOriginalUrl(), total, perDay, topReferrers);
    }
}