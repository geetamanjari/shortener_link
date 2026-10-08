package com.example.shortener.link;

import java.time.LocalDate;

public record DailyClicks(LocalDate date, long clicks) {
}
