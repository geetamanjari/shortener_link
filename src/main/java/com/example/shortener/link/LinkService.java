package com.example.shortener.link;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LinkService {

    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 7;

    private final SecureRandom random = new SecureRandom();
    private final LinkRepository linkRepository;
    private final LinkLookupService lookupService;

    @Transactional
    public Link create(CreateLinkRequest req) {
        String code;
        if (req.alias() != null && !req.alias().isBlank()) {
            if (linkRepository.existsByCode(req.alias())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Alias already taken");
            }
            code = req.alias();
        } else {
            code = generateUniqueCode();
        }

        Link link = new Link();
        link.setCode(code);
        link.setOriginalUrl(req.url());
        link.setExpiresAt(req.expiresAt());
        return linkRepository.save(link);
    }

    public LinkTarget resolve(String code) {
        LinkTarget target = lookupService.lookup(code);
        if (target.expiresAt() != null && target.expiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Link has expired");
        }
        return target;
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            String code = sb.toString();
            if (!linkRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate a unique code");
    }
}