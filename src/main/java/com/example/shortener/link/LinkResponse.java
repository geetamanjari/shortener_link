package com.example.shortener.link;

import java.time.Instant;

public record LinkResponse(
        String code,
        String shortUrl,
        String originalUrl,
        Instant expiresAt
) {
}