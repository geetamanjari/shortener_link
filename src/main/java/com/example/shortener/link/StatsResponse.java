package com.example.shortener.link;

import java.util.List;

public record StatsResponse(
        String code,
        String originalUrl,
        long totalClicks,
        List<DailyClicks> clicksPerDay,
        List<ReferrerCount> topReferrers
) {
}