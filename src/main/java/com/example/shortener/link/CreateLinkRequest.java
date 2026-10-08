package com.example.shortener.link;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

public record CreateLinkRequest(
        @NotBlank
        @Pattern(regexp = "^https?://.+", message = "must start with http:// or https://")
        String url,

        @Pattern(regexp = "^[A-Za-z0-9_-]{3,32}$", message = "3-32 chars: letters, digits, _ or -")
        String alias,

        Instant expiresAt
) {
}