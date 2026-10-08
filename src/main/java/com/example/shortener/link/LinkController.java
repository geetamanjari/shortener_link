package com.example.shortener.link;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Links", description = "Create short links")
@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Operation(summary = "Create a short link", description = "Optionally provide a custom alias and an expiry time")
    @PostMapping
    public ResponseEntity<LinkResponse> create(@Valid @RequestBody CreateLinkRequest req) {
        Link link = linkService.create(req);
        LinkResponse body = new LinkResponse(
                link.getCode(),
                baseUrl + "/" + link.getCode(),
                link.getOriginalUrl(),
                link.getExpiresAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}