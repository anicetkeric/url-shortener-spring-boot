package com.boottechsolutions.urlshortener.service;

import com.boottechsolutions.urlshortener.config.UrlShortenerProperties;
import com.boottechsolutions.urlshortener.domain.UrlMapping;
import com.boottechsolutions.urlshortener.dto.ShortenRequest;
import com.boottechsolutions.urlshortener.dto.ShortenResponse;
import com.boottechsolutions.urlshortener.dto.UrlStatsResponse;
import com.boottechsolutions.urlshortener.exception.UrlNotFoundException;
import com.boottechsolutions.urlshortener.repository.UrlMappingRepository;
import com.boottechsolutions.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerService {

    private static final String CACHE_PREFIX = "url:";

    private final UrlMappingRepository repository;
    private final RedisTemplate<String, String> redisTemplate;
    private final Base62Encoder encoder;
    private final ClickTrackingService clickTrackingService;
    private final UrlShortenerProperties properties;

    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {
        // Use a UUID-based placeholder so the NOT NULL constraint is satisfied on INSERT.
        // JPA dirty-checking replaces it with the Base62-encoded ID before the transaction commits.
        UrlMapping mapping = repository.saveAndFlush(
                UrlMapping.builder()
                        .originalUrl(request.url())
                        .shortCode(UUID.randomUUID().toString().replace("-", "").substring(0, 10))
                        .build()
        );

        String shortCode = encoder.encode(mapping.getId());
        mapping.setShortCode(shortCode);

        cacheUrl(shortCode, request.url());

        log.info("Shortened url={} shortCode={} id={}", request.url(), shortCode, mapping.getId());

        return new ShortenResponse(
                shortCode,
                properties.baseUrl() + "/" + shortCode,
                request.url(),
                mapping.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public String resolveUrl(String shortCode) {
        String cached = redisTemplate.opsForValue().get(CACHE_PREFIX + shortCode);

        if (cached != null) {
            log.debug("Cache hit shortCode={}", shortCode);
            clickTrackingService.recordClick(shortCode);
            return cached;
        }

        log.debug("Cache miss shortCode={}", shortCode);

        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        cacheUrl(shortCode, mapping.getOriginalUrl());
        clickTrackingService.recordClick(shortCode);

        return mapping.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    public UrlStatsResponse getStats(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        return new UrlStatsResponse(
                mapping.getShortCode(),
                mapping.getOriginalUrl(),
                mapping.getClickCount(),
                mapping.getCreatedAt()
        );
    }

    private void cacheUrl(String shortCode, String originalUrl) {
        redisTemplate.opsForValue().set(
                CACHE_PREFIX + shortCode,
                originalUrl,
                Duration.ofHours(properties.cacheTtlHours())
        );
    }
}
