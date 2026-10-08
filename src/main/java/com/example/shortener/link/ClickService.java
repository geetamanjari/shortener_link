package com.example.shortener.link;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClickService {

    private final ClickEventRepository clickEventRepository;
    private final LinkRepository linkRepository;

    @Async
    @Transactional
    public void recordClick(Long linkId, String referrer, String userAgent) {
        ClickEvent event = new ClickEvent();
        event.setLink(linkRepository.getReferenceById(linkId));
        event.setReferrer(truncate(referrer));
        event.setUserAgent(truncate(userAgent));
        clickEventRepository.save(event);
    }

    private String truncate(String value) {
        if (value == null) return null;
        return value.length() > 255 ? value.substring(0, 255) : value;
    }
}