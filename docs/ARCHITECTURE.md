# Architecture

## Overview

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Frontend   │────▶│   Inventory │     │   Payment   │
│  (React)    │     │   Service   │     │   Service   │
│  :3000      │     │   :8083     │     │   :8084     │
└─────────────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       │            ┌──────┴───────────────────┘
       │            │
       ▼            ▼
┌─────────────┐  ┌─────────────┐
│    Order    │  │    Kafka    │
│   Service   │◀─│   Cluster   │
│   :8086     │  │   :9092     │
└──────┬──────┘  └─────────────┘
       │
       ▼
┌─────────────┐
│   Jaeger    │
│   :16686    │
└─────────────┘
```

## Services

### Inventory Service (Port 8083)
- Product catalog management
- Category management
- Stock/volume tracking
- Database: PostgreSQL (port 5435)

### Order Service (Port 8086)
- Shopping cart operations
- Order lifecycle management
- Coordinates with payment via Kafka
- Database: PostgreSQL (port 5433)

### Payment Service (Port 8084)
- Payment processing
- Listens to order-created events
- Publishes payment status updates
- Database: PostgreSQL (port 5434)

### Frontend (Port 3000)
- React + Vite + TypeScript
- Communicates with all backend services

## Event Flow

### Purchase Saga (Choreography)

```
1. User creates order
   └─▶ Order Service publishes: order-created

2. Payment Service receives order-created
   └─▶ Processes payment
   └─▶ Publishes: order-payment (status: PAID/FAILED)

3. Order Service receives order-payment
   └─▶ Updates order status
```

## Kafka Topics

| Topic | Producer | Consumer | Event Type |
|-------|----------|----------|------------|
| `order-created` | Order | Payment | OrderCreatedEvent |
| `order-payment` | Payment | Order | OrderPaymentUpdatedEvent |
| `order-updated` | Order | - | OrderUpdatedEvent |
| `product-created` | Inventory | - | ProductCreatedEvent |
| `product-updated` | Inventory | Order | ProductUpdatedEvent |

## Databases

| Service | Database | Port | Schema |
|---------|----------|------|--------|
| Inventory | bookstore-inventory | 5435 | bookstore |
| Order | bookstore-order | 5433 | - |
| Payment | bookstore-payment | 5434 | bookstore |

## Observability

- **Tracing**: OpenTelemetry + Jaeger
- **Collector**: OTEL Collector (ports 4317/4318)
- **UI**: Jaeger at http://localhost:16686
- **Logs**: Include `traceId` and `spanId` for correlation
