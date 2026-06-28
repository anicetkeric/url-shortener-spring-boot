package com.boottechsolutions.urlshortener.dto;

import java.time.Instant;

public record UrlStatsResponse(
        String shortCode,
        String originalUrl,
        long clickCount,
        Instant createdAt
) {}
