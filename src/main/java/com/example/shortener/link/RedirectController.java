package com.example.shortener.link;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Redirect", description = "Resolve a short code")
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final LinkService linkService;
    private final ClickService clickService;

    @Operation(summary = "Redirect to the original URL", description = "Returns 302, 404 if unknown, 410 if expired. Records the click asynchronously.")
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(
            @PathVariable String code,
            @RequestHeader(value = HttpHeaders.REFERER, required = false) String referrer,
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {

        LinkTarget target = linkService.resolve(code);
        clickService.recordClick(target.id(), referrer, userAgent);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(target.originalUrl()))
                .build();
    }
}