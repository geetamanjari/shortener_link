package com.example.shortener.link;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analytics", description = "Click statistics")
@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "Get click statistics", description = "Total clicks, clicks per day and top referrers")
    @GetMapping("/{code}/stats")
    public StatsResponse stats(@PathVariable String code) {
        return statsService.getStats(code);
    }
}