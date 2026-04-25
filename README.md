# Microservice Online Store

Event-driven e-commerce platform with DDD, Kafka messaging, and choreography-based saga.

## Tech Stack

- **Backend:** Java 17+, Spring Boot, Kafka, PostgreSQL
- **Frontend:** React + Vite + TypeScript
- **Observability:** OpenTelemetry, Jaeger

[![video]]()](https://youtu.be/-p2X7TLY8EU)

## Services

| Service | Port | Description |
|---------|------|-------------|
| Frontend | 3000 | React storefront |
| Inventory | 8083 | Product catalog |
| Order | 8086 | Shopping cart & orders |
| Payment | 8084 | Payment processing |
| Kafka | 9092 | Event streaming |
| Jaeger | 16686 | Distributed tracing |

## Quick Start

```bash
docker-compose up -d
```

Open http://localhost:3000

## Development

```bash
# Run tests
./gradlew test

# Test specific service
./gradlew :inventory-microservice:test
./gradlew :order-microservice:test
./gradlew :payment-microservice:test

# Frontend
cd frontend && npm install && npm run dev
```

## Docker Build Performance Tips

If Docker builds look "stuck" at:

`RUN --mount=type=cache,... gradle ...`

it usually means Gradle is still resolving dependencies/compiling inside that step.

Use plain-progress logs:

```bash
docker-compose build --progress=plain order-microservice
```

Warm Gradle caches once, then rebuild:

```bash
docker-compose build order-microservice payment-microservice inventory-microservice
```

Notes:
- First build can be much slower than subsequent builds because dependencies and Gradle metadata are cached.
- Building one service at a time can be faster on low-memory Docker Desktop setups.
- Shared Gradle cache mount uses `sharing=locked` so parallel builds queue instead of failing on cache lock contention.

## Documentation

- [Architecture](docs/ARCHITECTURE.md) - System overview and event flows
- [API Reference](docs/API.md) - REST endpoints and Swagger links
- [Events](docs/EVENTS.md) - Kafka topics and Protobuf schemas

### Swagger UI

| Service | URL |
|---------|-----|
| Inventory | http://localhost:8083/swagger-ui.html |
| Order | http://localhost:8086/swagger-ui.html |
| Payment | http://localhost:8084/swagger-ui.html |

```bash
# Generate OpenAPI specs (auto-generated on build)
./gradlew generateOpenApiDocs
```

## License

MIT
