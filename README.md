# Microservice Online Store

Event-driven e-commerce platform with DDD, Kafka messaging, and choreography-based saga.

## Tech Stack

- **Backend:** Java 17+, Spring Boot, Kafka, PostgreSQL
- **Frontend:** React + Vite + TypeScript
- **Observability:** OpenTelemetry, Jaeger

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
# Generate OpenAPI specs
./scripts/generate-openapi.sh
```

## License

MIT
