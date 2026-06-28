package com.boottechsolutions.urlshortener.controller;

import com.boottechsolutions.urlshortener.dto.ShortenRequest;
import com.boottechsolutions.urlshortener.dto.ShortenResponse;
import com.boottechsolutions.urlshortener.dto.UrlStatsResponse;
import com.boottechsolutions.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlShortenerService service;

    @PostMapping
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.shorten(request));
    }

    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<UrlStatsResponse> getStats(@PathVariable String shortCode) {
        return ResponseEntity.ok(service.getStats(shortCode));
    }
}
