package com.boottechsolutions.urlshortener.service;

import com.boottechsolutions.urlshortener.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickTrackingService {

    private final UrlMappingRepository repository;

    @Async("clickTrackingExecutor")
    @Transactional
    public void recordClick(String shortCode) {
        try {
            repository.incrementClickCount(shortCode);
            log.debug("Recorded click for shortCode={}", shortCode);
        } catch (Exception ex) {
            // Click tracking is best-effort; do not fail the redirect
            log.warn("Failed to record click for shortCode={}: {}", shortCode, ex.getMessage());
        }
    }
}
