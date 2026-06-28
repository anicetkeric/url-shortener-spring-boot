package com.boottechsolutions.urlshortener.service;

import com.boottechsolutions.urlshortener.config.UrlShortenerProperties;
import com.boottechsolutions.urlshortener.domain.UrlMapping;
import com.boottechsolutions.urlshortener.dto.ShortenRequest;
import com.boottechsolutions.urlshortener.dto.ShortenResponse;
import com.boottechsolutions.urlshortener.dto.UrlStatsResponse;
import com.boottechsolutions.urlshortener.exception.UrlNotFoundException;
import com.boottechsolutions.urlshortener.repository.UrlMappingRepository;
import com.boottechsolutions.urlshortener.util.Base62Encoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock private UrlMappingRepository repository;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private Base62Encoder encoder;
    @Mock private ClickTrackingService clickTrackingService;
    @Mock private UrlShortenerProperties properties;

    @InjectMocks private UrlShortenerService service;

    private static final String BASE_URL = "http://localhost:8080";
    private static final String TEST_URL = "https://github.com/anicetkeric";

    @BeforeEach
    void setUp() {
        lenient().when(properties.baseUrl()).thenReturn(BASE_URL);
        lenient().when(properties.cacheTtlHours()).thenReturn(24L);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shorten_savesAndReturnsShortenedUrl() {
        UrlMapping saved = UrlMapping.builder()
                .id(1L)
                .originalUrl(TEST_URL)
                .shortCode("placeholder")
                .createdAt(Instant.now())
                .build();

        when(repository.saveAndFlush(any())).thenReturn(saved);
        when(encoder.encode(1L)).thenReturn("1");

        ShortenResponse response = service.shorten(new ShortenRequest(TEST_URL));

        assertThat(response.shortCode()).isEqualTo("1");
        assertThat(response.shortUrl()).isEqualTo(BASE_URL + "/1");
        assertThat(response.originalUrl()).isEqualTo(TEST_URL);
        assertThat(response.createdAt()).isNotNull();

        verify(valueOperations).set(eq("url:1"), eq(TEST_URL), any(Duration.class));
    }

    @Test
    void resolveUrl_cacheHit_returnsUrlWithoutDatabaseQuery() {
        when(valueOperations.get("url:abc")).thenReturn(TEST_URL);

        String result = service.resolveUrl("abc");

        assertThat(result).isEqualTo(TEST_URL);
        verify(repository, never()).findByShortCode(any());
        verify(clickTrackingService).recordClick("abc");
    }

    @Test
    void resolveUrl_cacheMiss_queriesDatabaseAndPopulatesCache() {
        when(valueOperations.get("url:abc")).thenReturn(null);
        when(repository.findByShortCode("abc")).thenReturn(Optional.of(
                UrlMapping.builder().shortCode("abc").originalUrl(TEST_URL).build()
        ));

        String result = service.resolveUrl("abc");

        assertThat(result).isEqualTo(TEST_URL);
        verify(repository).findByShortCode("abc");
        verify(valueOperations).set(eq("url:abc"), eq(TEST_URL), any(Duration.class));
        verify(clickTrackingService).recordClick("abc");
    }

    @Test
    void resolveUrl_notFound_throwsUrlNotFoundException() {
        when(valueOperations.get("url:xyz")).thenReturn(null);
        when(repository.findByShortCode("xyz")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveUrl("xyz"))
                .isInstanceOf(UrlNotFoundException.class)
                .hasMessageContaining("xyz");
    }

    @Test
    void getStats_returnsClickCountAndMetadata() {
        Instant now = Instant.now();
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("abc")
                .originalUrl(TEST_URL)
                .clickCount(42L)
                .createdAt(now)
                .build();

        when(repository.findByShortCode("abc")).thenReturn(Optional.of(mapping));

        UrlStatsResponse stats = service.getStats("abc");

        assertThat(stats.clickCount()).isEqualTo(42L);
        assertThat(stats.shortCode()).isEqualTo("abc");
        assertThat(stats.originalUrl()).isEqualTo(TEST_URL);
        assertThat(stats.createdAt()).isEqualTo(now);
    }

    @Test
    void getStats_unknownCode_throwsUrlNotFoundException() {
        when(repository.findByShortCode("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStats("nope"))
                .isInstanceOf(UrlNotFoundException.class);
    }
}
