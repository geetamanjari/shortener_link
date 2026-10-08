# URL Shortener with Click Analytics

A REST API built with Java and Spring Boot that shortens URLs, redirects visitors,
and records click analytics without slowing down the redirect.

## Features

- Create short links with auto-generated Base62 codes or custom aliases
- Optional link expiry (expired links return `410 Gone`)
- Fast `302` redirects; clicks are recorded asynchronously
- Stats per link: total clicks, clicks per day, top referrers
- Consistent JSON error responses with field-level validation messages
- Integration tests with MockMvc and interactive Swagger docs

## Tech stack

Java [17/21], Spring Boot 4.1, Spring Web MVC, Spring Data JPA, Bean Validation,
H2 (in-memory), Lombok, springdoc-openapi, JUnit 5 + MockMvc

## Run locally

```bash
./mvnw spring-boot:run
```

(On Windows: `mvnw.cmd spring-boot:run`, or run `ShortenerApplication` from your IDE.)

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:shortener`, user `sa`, blank password)

## API

| Method | Path | Description |
|---|---|---|
| POST | `/api/links` | Create a short link |
| GET | `/{code}` | Redirect to the original URL (302) |
| GET | `/api/links/{code}/stats` | Click statistics |

### Create a link

```bash
curl -X POST http://localhost:8080/api/links \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.wikipedia.org","alias":"wiki","expiresAt":"2030-01-01T00:00:00Z"}'
```

`alias` and `expiresAt` are optional.

```json
{
  "code": "wiki",
  "shortUrl": "http://localhost:8080/wiki",
  "originalUrl": "https://www.wikipedia.org",
  "expiresAt": "2030-01-01T00:00:00Z"
}
```

### Get stats

```bash
curl http://localhost:8080/api/links/wiki/stats
```

```json
{
  "code": "wiki",
  "originalUrl": "https://www.wikipedia.org",
  "totalClicks": 4,
  "clicksPerDay": [{"date": "2026-10-08", "clicks": 4}],
  "topReferrers": [{"referrer": "direct", "clicks": 4}]
}
```

### Errors

All errors share one format:

```json
{
  "timestamp": "2026-10-08T11:42:58Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/links",
  "fieldErrors": { "url": "must start with http:// or https://" }
}
```

| Status | When |
|---|---|
| 400 | Invalid or malformed request body |
| 404 | Unknown short code |
| 409 | Alias already taken |
| 410 | Link has expired |

## Design decisions

- **Async click logging:** the redirect returns immediately and the click is written
  on a background thread (`@Async`), so analytics never add latency to redirects.
- **Random Base62 codes with collision check:** 7 characters give about 3.5 trillion
  combinations; creation retries on the rare collision, and the unique index on `code`
  is the final safeguard.
- **302 (not 301) redirects:** browsers don't cache them, so every click is counted
  and expiry takes effect immediately.
- **Stats work on expired links:** analytics stay available after a link expires.

## Tests

```bash
./mvnw test
```

9 integration tests cover creation, validation, duplicate aliases, redirects,
expiry, unknown codes, and async click counting.

## Possible improvements

- PostgreSQL via Docker Compose for persistence across restarts
- Redis caching for the redirect lookup (read-heavy path)
- Rate limiting on link creation
- API key authentication and per-user link management