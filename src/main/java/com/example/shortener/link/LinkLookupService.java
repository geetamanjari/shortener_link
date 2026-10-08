package com.example.shortener.link;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LinkLookupService {

    private final LinkRepository linkRepository;

    @Cacheable(cacheNames = "links", key = "#code")
    @Transactional(readOnly = true)
    public LinkTarget lookup(String code) {
        Link link = linkRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found"));
        return new LinkTarget(link.getId(), link.getOriginalUrl(), link.getExpiresAt());
    }
}