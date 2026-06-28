# URL Shortener — Spring Boot 4 + Redis

Production-ready URL shortener built with Java 21, Spring Boot 4, PostgreSQL, and Redis.

## Tech Stack

- Java 21
- Spring Boot 4.0.0
- PostgreSQL 16 (persistence)
- Redis 7 (cache-aside)
- Flyway (database migrations)
- Docker Compose

## Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+

## Quick Start

**1. Start infrastructure:**

```bash
docker compose up -d
```

**2. Run the application:**

```bash
./mvnw spring-boot:run
```

**3. Shorten a URL:**

```bash
curl -s -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"url": "https://github.com/anicetkeric"}' | jq .
```

**4. Follow the redirect:**

```bash
curl -v http://localhost:8080/3
```

**5. Get click statistics:**

```bash
curl -s http://localhost:8080/api/v1/urls/3/stats | jq .
```

## API

| Method | Endpoint                         | Description              |
|--------|----------------------------------|--------------------------|
| POST   | `/api/v1/urls`                   | Shorten a URL            |
| GET    | `/{shortCode}`                   | Redirect to original URL |
| GET    | `/api/v1/urls/{shortCode}/stats` | Get click statistics     |

## Project Structure

```
src/main/java/com/boottechsolutions/urlshortener/
├── config/
│   ├── AsyncConfig.java            # Async executor for click tracking
│   ├── RedisConfig.java            # RedisTemplate bean
│   └── UrlShortenerProperties.java # @ConfigurationProperties record
├── controller/
│   ├── RedirectController.java     # GET /{shortCode} → 302 redirect
│   └── UrlController.java          # POST /api/v1/urls, GET stats
├── domain/
│   └── UrlMapping.java             # JPA entity
├── dto/
│   ├── ErrorResponse.java
│   ├── ShortenRequest.java
│   ├── ShortenResponse.java
│   └── UrlStatsResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── UrlNotFoundException.java
├── repository/
│   └── UrlMappingRepository.java
├── service/
│   ├── ClickTrackingService.java   # Async click count increment
│   └── UrlShortenerService.java    # Core business logic
└── util/
    └── Base62Encoder.java          # ID → short code encoding
```

## Running Tests

```bash
./mvnw test
```
