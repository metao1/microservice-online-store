[![Java CI with Gradle](https://github.com/metao1/microservice-online-book-store/actions/workflows/gradle.yml/badge.svg)](https://github.com/metao1/microservice-online-book-store/actions/workflows/gradle.yml) [![Frontend tests](https://github.com/metao1/microservice-online-book-store/actions/workflows/frontend-tests.yml/badge.svg)](https://github.com/metao1/microservice-online-book-store/actions/workflows/frontend-tests.yml) [![Test Coverage Validation](https://github.com/metao1/microservice-online-book-store/actions/workflows/test-coverage.yml/badge.svg)](https://github.com/metao1/microservice-online-book-store/actions/workflows/test-coverage.yml)

# Microservice Online Store

Event-driven e‑commerce platform built with DDD aggregates, Kafka messaging, and a choreography-based saga across payment, order, and inventory services. See `docs/ARCHITECTURE.md` for the current message flows and per-service diagrams.

## Prerequisites
- Docker & Docker Compose
- Java 17+ (for local builds/tests)
- Node.js 18+ (if you want to run the React frontend outside Compose)

## Run with Docker Compose
This is the recommended way to start the full stack (infrastructure, microservices, and frontend).

```bash
docker-compose up -d
```

Services and ports:
- Zookeeper (2181), Kafka (9092/9094), Schema Registry (8081)
- PostgreSQL for inventory (5432) and order (5433)
- Inventory service (8083), Order service (8080), Payment service (8084)
- React frontend (3000) at http://localhost:3000

Common commands:
- `docker-compose logs -f` – follow logs
- `docker-compose ps` – check health/status
- `docker-compose down -v` – stop and remove volumes

## Testing
```bash
# All services
./gradlew test

# Specific microservice
./gradlew :inventory-microservice:test
./gradlew :order-microservice:test
./gradlew :payment-microservice:test
```

## Architecture
Diagrams, message contracts, and sequence flows live in `docs/ARCHITECTURE.md`. Each microservice section shows the current implementation and Kafka topics involved.

## Troubleshooting
- Kafka topics: `docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092`
- Service health: `curl http://localhost:<port>/actuator/health`
- Database access: `psql -h localhost -p 5432 -U admin -d bookstore` (inventory), `-p 5433 -d bookstore-order` (order)

## Contributing
1. Create a feature branch.
2. Write tests with your changes.
3. Run `./gradlew test`.
4. Open a PR with context on the service(s) touched.

## License
MIT
