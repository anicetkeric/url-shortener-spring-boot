package com.boottechsolutions.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "url-shortener")
public record UrlShortenerProperties(
        String baseUrl,
        long cacheTtlHours
) {}
