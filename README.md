 [![Java CI with Gradle](https://github.com/metao1/microservice-online-store/actions/workflows/gradle.yml/badge.svg)](https://github.com/metao1/microservice-online-store/actions/workflows/gradle.yml) [![Frontend tests](https://github.com/metao1/microservice-online-store/actions/workflows/frontend-tests.yml/badge.svg)](https://github.com/metao1/microservice-online-store/actions/workflows/frontend-tests.yml) [![Test Coverage Validation](https://github.com/metao1/microservice-online-store/actions/workflows/test-coverage.yml/badge.svg)](https://github.com/metao1/microservice-online-store/actions/workflows/test-coverage.yml)

# Microservice Online Store

A modern event-driven e-commerce platform built with microservices architecture, implementing Domain-Driven Design (DDD) principles and leveraging Apache Kafka for asynchronous communication between services.

## Overview

This project is a production-ready e-commerce system that demonstrates best practices in microservices architecture, including:

- **Event-Driven Architecture**: Services communicate asynchronously via Apache Kafka
- **Domain-Driven Design**: Rich domain models with aggregates, entities, and value objects
- **Saga Pattern**: Choreography-based saga for order-payment coordination
- **Event Sourcing**: All state changes captured as domain events
- **Database per Service**: Each microservice owns its data
- **Protocol Buffers**: Efficient event serialization with schema validation
- **Clean Architecture**: Clear separation of concerns across layers

## Architecture

The system consists of three core microservices that communicate through Kafka events:

![Event Storming](img/eventstorming.png)

![Online Shop Architecture](img/online-shop-architecture.png)

![Component and Sequence Diagrams](img/order-payment-inventory-user-diagrams.png)

For detailed architecture documentation, including sequence diagrams, event flows, API endpoints, and DDD patterns, see [architecture](docs/ARCHITECTURE.md).

## Technology Stack

### Backend
- **Java 17+** - Programming language
- **Spring Boot 3.x** - Application framework
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM with 2nd level caching (Caffeine)
- **Gradle** - Build automation
- **Flyway** - Database migrations

### Messaging & Events
- **Apache Kafka** - Message broker
- **Zookeeper** - Kafka coordination
- **Confluent Schema Registry** - Schema management
- **Protocol Buffers (Protobuf)** - Event serialization

### Databases
- **PostgreSQL** - Primary database for all services
- **HikariCP** - Database connection pooling

### Frontend
- **React** - UI framework
- **JavaScript/ES6** - Programming language

### Infrastructure
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

## Getting Started

### Prerequisites

- **Java 17+**
- **Docker & Docker Compose**
- **Node.js 14+** (for frontend)
- **Gradle** (wrapper included)

### Quick Start with Docker Compose

Start all services (infrastructure + microservices):

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Check service health
docker-compose ps
```

This will start:
- Zookeeper (port 2181)
- Kafka (ports 9092, 9094)
- Schema Registry (port 8081)
- PostgreSQL instances (ports 5432, 5433)
- Product Microservice (port 8083)
- Order Microservice (port 8080)
- Payment Microservice (port 8084)
- React Frontend (port 3000)

**Access the application**: http://localhost:3000

### Local Development Setup

#### Start Microservices and Infrastructure

```bash
# Start only Kafka, Zookeeper, PostgreSQL, Schema Registry
docker-compose up -d zookeeper kafka schema-registry postgres postgres-order
```

#### 2. Run Microservices Locally

```bash
# Terminal 1 - Product Microservice
./gradlew :inventory-microservice:bootRun

# Terminal 2 - Order Microservice
./gradlew :order-microservice:bootRun

# Terminal 3 - Payment Microservice
./gradlew :payment-microservice:bootRun
```

#### 3. Run Frontend

```bash
cd frontend
npm install
npm start
```

### Verify Setup

Check service health:

```bash
# Product service
curl http://localhost:8083/actuator/health

# Order service
curl http://localhost:8080/actuator/health

# Payment service
curl http://localhost:8084/actuator/health

# Kafka topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

## Testing

### Run All Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific microservice
./gradlew :inventory-microservice:test
./gradlew :order-microservice:test
./gradlew :payment-microservice:test
```

### Integration Tests

```bash
# Run integration tests (uses TestContainers)
./gradlew integrationTest

# Run specific integration test
./gradlew :order-microservice:test --tests "*OrderManagementControllerIT"
```

### Test Coverage

Coverage is validated in CI for the inventory microservice by the "Test Coverage Validation" GitHub Actions workflow (`.github/workflows/test-coverage.yml`). It runs `inventory-microservice:test`, generates the JaCoCo XML report, and uploads it to Codecov.

```bash
# Generate test coverage report (root + service-specific)
./gradlew jacocoTestReport
./gradlew :inventory-microservice:jacocoTestReport
./gradlew :order-microservice:jacocoTestReport

# View report at: build/reports/jacoco/test/html/index.html
```

## Event-Driven Flow

### Order Processing Saga (Choreography Pattern)

**Happy Path**:
1. User adds items to cart → Shopping cart updated
2. User creates order → Order status: `CREATED`
3. `OrderCreatedEvent` published to Kafka
4. Payment service processes payment (80% success)
5. `OrderPaymentEvent` published with status `SUCCESSFUL`
6. Order service updates order → Order status: `PAID`
7. Order proceeds to `SHIPPED` → `DELIVERED`

**Compensation Path**:
1. Payment fails (20% chance)
2. `OrderPaymentEvent` published with status `FAILED`
3. Order service updates order → Order status: `PAYMENT_FAILED`
4. User notified to retry payment

## Monitoring and Operations

### Health Checks

```bash
# Service health
GET /actuator/health

# Application info
GET /actuator/info

# Metrics
GET /actuator/metrics
```

### Kafka Monitoring

- **Kafka Topics UI**: http://localhost:8000
- **Schema Registry UI**: http://localhost:8001

### Database Access

```bash
# Product database
psql -h localhost -p 5432 -U admin -d bookstore

# Order database
psql -h localhost -p 5433 -U admin -d bookstore-order
```

## Troubleshooting

### Kafka Connection Issues

```bash
# Check Kafka is running
docker ps | grep kafka

# View Kafka logs
docker logs kafka

# Verify topics exist
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Database Migration Issues

```bash
# Check Flyway migrations
./gradlew :inventory-microservice:flywayInfo

# Repair failed migrations
./gradlew :inventory-microservice:flywayRepair
```

### Service Won't Start

```bash
# Check port conflicts
lsof -i :8080
lsof -i :8083
lsof -i :8084

# View service logs
./gradlew :order-microservice:bootRun --debug
```

## Documentation

- **[Architecture](docs/ARCHITECTURE.md)** - Detailed architecture documentation with:
  - Complete sequence diagrams for all flows
  - Event catalog and Kafka topics
  - REST API endpoints for all services
  - Domain-Driven Design patterns
  - Project structure and modules
  - Deployment and scaling strategies

## Development Best Practices

- **Keep Aggregates Small**: Single responsibility, clear boundaries
- **Validate in Value Objects**: Fail fast on construction
- **Publish Events in Transactions**: Ensure consistency
- **Make Event Handlers Idempotent**: Handle duplicate events gracefully
- **Test Domain Logic Thoroughly**: High coverage for business rules

## Future Enhancements
- [ ] Distributed Caching (Redis)
- [ ] User/Authentication Service (OAuth2/JWT)
- [ ] Notification Service (Email, SMS)
- [ ] Monitoring Stack (Prometheus, Grafana, ELK, Jaeger)
- [ ] Real Payment Gateway Integration (Stripe/PayPal)

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Domain-Driven Design by Eric Evans
- Implementing Domain-Driven Design by Vaughn Vernon
- Building Microservices by Sam Newman
- Enterprise Integration Patterns by Gregor Hohpe

---

**Built with a passion for clean architecture and domain-driven design**
