package com.example.shortener.link;

import java.io.Serializable;
import java.time.Instant;

public record LinkTarget(Long id, String originalUrl, Instant expiresAt) implements Serializable {
}