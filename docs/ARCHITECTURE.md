# Microservices Architecture - Online Store

## System Architecture Overview

This document describes the complete microservices architecture for the online store application based on the actual implementation, including all service interactions, message flows, and infrastructure components.

## Technology Stack

- **Language**: Java 17+
- **Framework**: Spring Boot 3.x
- **Build Tool**: Gradle
- **Databases**: PostgreSQL (per service)
- **Message Broker**: Apache Kafka with Schema Registry
- **Event Serialization**: Protocol Buffers (Protobuf)
- **ORM**: Hibernate/JPA
- **Database Migration**: Flyway
- **Frontend**: React
- **Containerization**: Docker, Docker Compose

## Service Maps (per microservice)

### Inventory / Product Service (port 8083)

```mermaid
graph LR
    UI[Web / Admin UI] -->|REST| ProdAPI(ProductController)
    ProdAPI --> Mapper(ProductApplicationMapper)
    Mapper --> Domain(ProductAggregate)
    Domain --> Repo[(PostgreSQL: product_table)]
    Domain --> Events(ProductDomainEvents)
    Events -->|Protobuf| Kafka((Kafka))
    Kafka -->|product-updated| InventoryListener(ProductKafkaListenerComponent)
```

Notes:
- Controller delegates to `ProductDomainService` via mapper; idempotency support persists to `product_create_request`.
- `ProductKafkaListenerComponent` consumes `product-updated` events with `INVENTORY_REDUCTION` markers to adjust stock.
- Categories are natural-ID cached (`CategoryEntityMapper` uses Hibernate simple natural ID).

### Order Service (port 8080)

```mermaid
graph LR
    UI -->|REST| OrderAPI(OrderManagementController)
    UI -->|REST| CartAPI(ShoppingCartController)
    OrderAPI --> OrderSvc(OrderManagementService)
    CartAPI --> CartSvc(ShoppingCartService)
    OrderSvc --> OrderAgg(OrderAggregate)
    OrderAgg --> OrderRepo[(PostgreSQL: order_table)]
    OrderAgg --> OrderEvents(DomainOrder events)
    OrderEvents -->|order-created-events| Kafka((Kafka))
    Kafka -->|order-payment-events| PaymentListener(PaymentEventListener)
    PaymentListener --> OrderSvc
    OrderAgg -.-> InventoryMarker(DomainInventoryReductionRequestedEvent)
    InventoryMarker --> ProductUpdatedTranslator(ProductUpdatedEventTranslator)
    ProductUpdatedTranslator -->|product-updated| Kafka
```

Notes:
- Adding the same SKU merges quantities instead of erroring.
- `updateItemQuantity` emits inventory-reduction markers via `product-updated` topic for inventory service to consume.

### Payment Service (port 8084)

```mermaid
graph LR
    Kafka((Kafka)) -->|order-created-events| OrderCreatedListener
    OrderCreatedListener --> PayApp(PaymentApplicationService)
    PayApp --> PayProc(PaymentProcessingService)
    PayProc --> PayAgg(PaymentAggregate)
    PayAgg --> PayRepo[(PostgreSQL: payment_table)]
    PayProc --> PayEvents(OrderPaymentEvent)
    PayEvents -->|order-payment-events| Kafka
```

Notes:
- `PaymentProcessingService` deterministically maps `PaymentApplicationService` result to SUCCESSFUL/FAILED `OrderPaymentEvent`.


## Complete Order Processing Saga Flow

This is the actual implemented choreography-based saga for order processing with payment:

```mermaid
sequenceDiagram
    actor User
    participant WebApp as React Web App
    participant CartAPI as Shopping Cart API<br/>(OrderMS:8080)
    participant OrderAPI as Order API<br/>(OrderMS:8080)
    participant OrderService as OrderManagementService
    participant OrderAggregate as Order Aggregate
    participant OrderRepo as Order Repository<br/>(PostgreSQL:5433)
    participant EventHandler as DomainEventToKafkaEventHandler
    participant Kafka as Apache Kafka
    participant OrderCreatedListener as OrderCreatedEventListener<br/>(PaymentMS)
    participant PaymentService as PaymentApplicationService
    participant PaymentAggregate as Payment Aggregate
    participant PaymentRepo as Payment Repository
    participant PaymentEventPublisher as Payment Event Publisher
    participant PaymentListener as PaymentEventListener<br/>(OrderMS)

    %% Shopping Cart Phase
    User->>WebApp: Add items to cart
    WebApp->>CartAPI: POST /cart
    activate CartAPI
    CartAPI->>CartService: addItemsToCart(userId, items)
    CartService-->>WebApp: Cart updated
    deactivate CartAPI

    User->>WebApp: View cart & checkout
    WebApp->>CartAPI: GET /cart/{userId}
    activate CartAPI
    CartAPI-->>WebApp: Return cart items
    deactivate CartAPI

    %% Order Creation Phase
    User->>WebApp: Create Order
    WebApp->>OrderAPI: POST /api/order<br/>{customerId, items}
    activate OrderAPI
    OrderAPI->>OrderService: createOrder(command)
    activate OrderService

    OrderService->>OrderAggregate: Create Order Aggregate
    activate OrderAggregate
    Note over OrderAggregate: Status: CREATED<br/>Raises DomainOrderCreatedEvent
    OrderAggregate-->>OrderService: Order Created
    deactivate OrderAggregate

    OrderService->>OrderRepo: save(order)
    activate OrderRepo
    OrderRepo-->>OrderService: Order saved
    deactivate OrderRepo

    OrderService->>EventHandler: Publish domain events
    activate EventHandler
    EventHandler->>EventHandler: Translate to OrderCreatedEvent (Protobuf)
    EventHandler->>Kafka: Publish to "order-created-events" topic
    EventHandler-->>OrderService: Event published
    deactivate EventHandler

    OrderService-->>OrderAPI: OrderDTO
    deactivate OrderService
    OrderAPI-->>WebApp: 201 Created (Order ID)
    deactivate OrderAPI
    WebApp-->>User: Order confirmation page

    %% Payment Processing Phase
    Kafka->>OrderCreatedListener: OrderCreatedEvent consumed
    activate OrderCreatedListener
    Note over OrderCreatedListener: @KafkaListener on<br/>"order-created-events" topic

    OrderCreatedListener->>PaymentService: processPayment(orderId, amount)
    activate PaymentService

    PaymentService->>PaymentAggregate: Create Payment Aggregate
    activate PaymentAggregate
    Note over PaymentAggregate: Status: PENDING<br/>PaymentMethod configured
    PaymentAggregate-->>PaymentService: Payment created
    deactivate PaymentAggregate

    PaymentService->>PaymentRepo: save(payment)
    activate PaymentRepo
    PaymentRepo-->>PaymentService: Payment saved
    deactivate PaymentRepo

    PaymentService->>PaymentAggregate: processPayment()
    activate PaymentAggregate
    Note over PaymentAggregate: Simulates payment gateway<br/>80% success rate

    alt Payment Successful
        PaymentAggregate->>PaymentAggregate: markAsSuccessful()
        Note over PaymentAggregate: Status: SUCCESSFUL<br/>Raises PaymentProcessedEvent
        PaymentAggregate-->>PaymentService: Payment successful

        PaymentService->>PaymentRepo: save(payment)
        activate PaymentRepo
        PaymentRepo-->>PaymentService: Updated
        deactivate PaymentRepo

        PaymentService->>PaymentEventPublisher: publishPaymentProcessedEvent()
        activate PaymentEventPublisher
        PaymentEventPublisher->>Kafka: Publish OrderPaymentEvent<br/>(status: SUCCESSFUL)
        Note over Kafka: Published to<br/>"order-payment-events" topic
        deactivate PaymentEventPublisher

    else Payment Failed (20% chance)
        PaymentAggregate->>PaymentAggregate: markAsFailed()
        Note over PaymentAggregate: Status: FAILED<br/>Raises PaymentFailedEvent
        PaymentAggregate-->>PaymentService: Payment failed

        PaymentService->>PaymentRepo: save(payment)
        activate PaymentRepo
        PaymentRepo-->>PaymentService: Updated
        deactivate PaymentRepo

        PaymentService->>PaymentEventPublisher: publishPaymentFailedEvent()
        activate PaymentEventPublisher
        PaymentEventPublisher->>Kafka: Publish OrderPaymentEvent<br/>(status: FAILED)
        Note over Kafka: Published to<br/>"order-payment-events" topic
        deactivate PaymentEventPublisher
    end

    deactivate PaymentAggregate
    PaymentService-->>OrderCreatedListener: Processing complete
    deactivate PaymentService
    deactivate OrderCreatedListener

    %% Order Status Update Phase
    Kafka->>PaymentListener: OrderPaymentEvent consumed
    activate PaymentListener
    Note over PaymentListener: @KafkaListener on<br/>"order-payment-events" topic

    PaymentListener->>OrderService: updateOrderStatus(orderId, paymentStatus)
    activate OrderService

    OrderService->>OrderRepo: findById(orderId)
    activate OrderRepo
    OrderRepo-->>OrderService: Order entity
    deactivate OrderRepo

    alt Payment Successful
        OrderService->>OrderAggregate: updateStatus(PAID)
        activate OrderAggregate
        Note over OrderAggregate: Status: CREATED → PAID<br/>Status transition validated<br/>Raises OrderStatusChangedEvent
        OrderAggregate-->>OrderService: Status updated
        deactivate OrderAggregate

        OrderService->>OrderRepo: save(order)
        activate OrderRepo
        OrderRepo-->>OrderService: Order updated
        deactivate OrderRepo

        OrderService->>EventHandler: Publish OrderStatusChangedEvent
        activate EventHandler
        EventHandler->>Kafka: Publish to "order-updated" topic
        deactivate EventHandler

        Note over User: User can now see<br/>Order Status: PAID

    else Payment Failed
        OrderService->>OrderAggregate: updateStatus(PAYMENT_FAILED)
        activate OrderAggregate
        Note over OrderAggregate: Status: PAYMENT_FAILED<br/>Order marked as failed
        OrderAggregate-->>OrderService: Status updated
        deactivate OrderAggregate

        OrderService->>OrderRepo: save(order)
        activate OrderRepo
        OrderRepo-->>OrderService: Order updated
        deactivate OrderRepo

        Note over User: User notified of<br/>payment failure
    end

    deactivate OrderService
    deactivate PaymentListener
```

## Product Management Flow

```mermaid
sequenceDiagram
    actor Admin
    participant WebApp as React Web App
    participant ProductAPI as Product API<br/>(ProductMS:8083)
    participant ProductService as ProductApplicationService
    participant ProductDomain as Product Aggregate
    participant ProductRepo as Product Repository<br/>(PostgreSQL:5432)
    participant EventPublisher as Domain Event Publisher
    participant Kafka as Apache Kafka
    participant ProductListener as ProductKafkaListener

    %% Product Creation
    Admin->>WebApp: Create new product
    WebApp->>ProductAPI: POST /products<br/>{sku, title, description, price, volume}
    activate ProductAPI

    ProductAPI->>ProductService: createProduct(command)
    activate ProductService

    ProductService->>ProductDomain: Create Product Aggregate
    activate ProductDomain
    Note over ProductDomain: Validates:<br/>- ProductSku uniqueness<br/>- Money (price, VAT)<br/>- Quantity<br/>- ImageUrl format<br/>Raises ProductCreatedEvent
    ProductDomain-->>ProductService: Product created
    deactivate ProductDomain

    ProductService->>ProductRepo: save(product)
    activate ProductRepo
    ProductRepo-->>ProductService: Product saved
    deactivate ProductRepo

    ProductService->>EventPublisher: Publish ProductCreatedEvent
    activate EventPublisher
    EventPublisher->>Kafka: Publish to "product-created" topic<br/>(Protobuf serialized)
    deactivate EventPublisher

    ProductService-->>ProductAPI: ProductDTO
    deactivate ProductService
    ProductAPI-->>WebApp: 201 Created
    deactivate ProductAPI

    %% Event Processing
    Kafka->>ProductListener: ProductCreatedEvent consumed
    activate ProductListener
    Note over ProductListener: Event consumed for:<br/>- Audit logging<br/>- Cache warming<br/>- Analytics
    deactivate ProductListener

    %% Product Update
    Admin->>WebApp: Update product details
    WebApp->>ProductAPI: PUT /products/{sku}<br/>{title, description, price}
    activate ProductAPI

    ProductAPI->>ProductService: updateProduct(sku, command)
    activate ProductService

    ProductService->>ProductRepo: findBySku(sku)
    activate ProductRepo
    ProductRepo-->>ProductService: Product aggregate
    deactivate ProductRepo

    ProductService->>ProductDomain: updatePrice(newPrice)
    activate ProductDomain
    Note over ProductDomain: Validates price change<br/>Updates Money value object<br/>Raises ProductUpdatedEvent
    deactivate ProductDomain

    ProductService->>ProductDomain: updateTitle(newTitle)
    activate ProductDomain
    Note over ProductDomain: Updates ProductTitle<br/>Raises ProductUpdatedEvent
    deactivate ProductDomain

    ProductService->>ProductRepo: save(product)
    activate ProductRepo
    ProductRepo-->>ProductService: Product saved
    deactivate ProductRepo

    ProductService->>EventPublisher: Publish ProductUpdatedEvent
    activate EventPublisher
    EventPublisher->>Kafka: Publish to "product-updated" topic
    deactivate EventPublisher

    ProductService-->>ProductAPI: ProductDTO
    deactivate ProductService
    ProductAPI-->>WebApp: 200 OK
    deactivate ProductAPI

    Kafka->>ProductListener: ProductUpdatedEvent consumed
    activate ProductListener
    Note over ProductListener: Update caches,<br/>search indexes,<br/>analytics
    deactivate ProductListener

    %% Category Assignment
    Admin->>WebApp: Assign product to category
    WebApp->>ProductAPI: POST /products/{sku}/categories/{categoryName}
    activate ProductAPI

    ProductAPI->>ProductService: assignProductToCategory(sku, categoryName)
    activate ProductService

    ProductService->>ProductDomain: addCategory(categoryEntity)
    activate ProductDomain
    Note over ProductDomain: Validates category<br/>Prevents duplicates<br/>Updates product-category mapping
    ProductDomain-->>ProductService: Category added
    deactivate ProductDomain

    ProductService->>ProductRepo: save(product)
    activate ProductRepo
    ProductRepo-->>ProductService: Product saved
    deactivate ProductRepo

    ProductService-->>ProductAPI: Success
    deactivate ProductService
    ProductAPI-->>WebApp: 200 OK
    deactivate ProductAPI

    %% Inventory Management
    Admin->>WebApp: Reduce inventory
    WebApp->>ProductAPI: POST /products/{sku}/volume/reduce<br/>{quantity}
    activate ProductAPI

    ProductAPI->>ProductService: reduceProductVolume(sku, quantity)
    activate ProductService

    ProductService->>ProductDomain: reduceVolume(quantity)
    activate ProductDomain
    Note over ProductDomain: Validates:<br/>- Sufficient stock available<br/>- Quantity > 0<br/>Updates Quantity<br/>Raises ProductUpdatedEvent

    alt Sufficient Stock
        ProductDomain-->>ProductService: Volume reduced
    else Insufficient Stock
        ProductDomain-->>ProductService: Error: Not enough stock
    end
    deactivate ProductDomain

    ProductService->>ProductRepo: save(product)
    ProductService-->>ProductAPI: Result
    deactivate ProductService
    ProductAPI-->>WebApp: 200 OK or 400 Bad Request
    deactivate ProductAPI
```

## Shopping Cart Operations

```mermaid
sequenceDiagram
    actor User
    participant WebApp as React Web App
    participant CartAPI as Shopping Cart API<br/>(OrderMS:8080)
    participant CartService as ShoppingCartService
    participant CartRepo as ShoppingCartRepository<br/>(In-Memory)

    %% Add items to cart
    User->>WebApp: Add product to cart
    WebApp->>CartAPI: POST /cart<br/>{userId, sku, quantity}
    activate CartAPI

    CartAPI->>CartService: addItemsToCart(request)
    activate CartService

    CartService->>CartRepo: findByUserId(userId)
    activate CartRepo

    alt Cart exists
        CartRepo-->>CartService: Existing cart
        CartService->>CartService: Add/update item in cart
    else Cart doesn't exist
        CartRepo-->>CartService: null
        CartService->>CartService: Create new cart
    end
    deactivate CartRepo

    CartService->>CartRepo: save(cart)
    activate CartRepo
    CartRepo-->>CartService: Cart saved
    deactivate CartRepo

    CartService-->>CartAPI: CartDTO
    deactivate CartService
    CartAPI-->>WebApp: 200 OK
    deactivate CartAPI

    %% Get cart
    User->>WebApp: View cart
    WebApp->>CartAPI: GET /cart/{userId}
    activate CartAPI

    CartAPI->>CartService: getShoppingCart(userId)
    activate CartService

    CartService->>CartRepo: findByUserId(userId)
    activate CartRepo
    CartRepo-->>CartService: Cart with items
    deactivate CartRepo

    CartService-->>CartAPI: CartDTO
    deactivate CartService
    CartAPI-->>WebApp: 200 OK (cart items, total)
    deactivate CartAPI

    %% Update item quantity
    User->>WebApp: Update quantity
    WebApp->>CartAPI: PUT /cart/{userId}/{sku}<br/>{newQuantity}
    activate CartAPI

    CartAPI->>CartService: updateItemQuantity(userId, sku, quantity)
    activate CartService

    CartService->>CartRepo: findByUserId(userId)
    activate CartRepo
    CartRepo-->>CartService: Cart
    deactivate CartRepo

    CartService->>CartService: Update item quantity

    CartService->>CartRepo: save(cart)
    activate CartRepo
    CartRepo-->>CartService: Updated
    deactivate CartRepo

    CartService-->>CartAPI: CartDTO
    deactivate CartService
    CartAPI-->>WebApp: 200 OK
    deactivate CartAPI

    %% Remove item
    User->>WebApp: Remove item
    WebApp->>CartAPI: DELETE /cart/{userId}/{sku}
    activate CartAPI

    CartAPI->>CartService: removeItem(userId, sku)
    activate CartService

    CartService->>CartRepo: findByUserId(userId)
    activate CartRepo
    CartRepo-->>CartService: Cart
    deactivate CartRepo

    CartService->>CartService: Remove item from cart

    CartService->>CartRepo: save(cart)
    activate CartRepo
    CartRepo-->>CartService: Updated
    deactivate CartRepo

    CartService-->>CartAPI: Success
    deactivate CartService
    CartAPI-->>WebApp: 204 No Content
    deactivate CartAPI

    %% Clear cart
    User->>WebApp: Clear cart
    WebApp->>CartAPI: DELETE /cart/{userId}
    activate CartAPI

    CartAPI->>CartService: clearCart(userId)
    activate CartService

    CartService->>CartRepo: deleteByUserId(userId)
    activate CartRepo
    CartRepo-->>CartService: Deleted
    deactivate CartRepo

    CartService-->>CartAPI: Success
    deactivate CartService
    CartAPI-->>WebApp: 204 No Content
    deactivate CartAPI
```

## REST API Endpoints

### Product Microservice (Port 8083)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products/{sku}` | Get product by SKU |
| POST | `/products` | Create new product |
| PUT | `/products/{sku}` | Update product |
| GET | `/products/category/{categoryName}` | Get products by category (paginated) |
| GET | `/products/search?keyword={keyword}` | Search products |
| GET | `/products/{sku}/related` | Get related products |
| POST | `/products/{sku}/categories/{categoryName}` | Assign product to category |
| POST | `/products/{sku}/volume/reduce` | Reduce product inventory |
| POST | `/products/{sku}/volume/increase` | Increase product inventory |

### Order Microservice (Port 8080)

**Order Management:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/order` | Create new order |
| PUT | `/api/order/{orderId}/items` | Add items to order |
| PATCH | `/api/order/{orderId}/status` | Update order status |
| GET | `/api/order/customer/{customerId}` | Get customer orders |

**Shopping Cart:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/cart/{userId}` | Get shopping cart |
| POST | `/cart` | Add items to cart |
| PUT | `/cart/{userId}/{sku}` | Update item quantity |
| DELETE | `/cart/{userId}/{sku}` | Remove item from cart |
| DELETE | `/cart/{userId}` | Clear cart |

### Payment Microservice (Port 8084)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/payments` | Create payment |
| POST | `/payments/{paymentId}/process` | Process payment |
| POST | `/payments/{paymentId}/retry` | Retry failed payment |
| POST | `/payments/{paymentId}/cancel` | Cancel payment |
| GET | `/payments/{paymentId}` | Get payment by ID |
| GET | `/payments/order/{orderId}` | Get payment by order ID |
| GET | `/payments/status/{status}` | Get payments by status |
| GET | `/payments/statistics` | Get payment statistics |

## Event Catalog

### Product Events

**ProductCreatedEvent**
```protobuf
message ProductCreatedEvent {
  string product_id = 1;
  string sku = 2;
  string title = 3;
  double price = 4;
  int32 volume = 5;
  int64 timestamp = 6;
}
```

**ProductUpdatedEvent**
```protobuf
message ProductUpdatedEvent {
  string product_id = 1;
  string sku = 2;
  string title = 3;
  double price = 4;
  int64 timestamp = 5;
}
```

### Order Events

**OrderCreatedEvent**
```protobuf
message OrderCreatedEvent {
  string order_id = 1;
  string customer_id = 2;
  repeated OrderItem items = 3;
  double total_amount = 4;
  string status = 5;
  int64 timestamp = 6;
}
```

**OrderPaymentEvent**
```protobuf
message OrderPaymentEvent {
  string order_id = 1;
  string payment_id = 2;
  string status = 3;  // SUCCESSFUL or FAILED
  double amount = 4;
  int64 timestamp = 5;
}
```

### Payment Events

**PaymentProcessedEvent** (Domain Event)
- Raised when payment succeeds
- Translated to OrderPaymentEvent for Kafka

**PaymentFailedEvent** (Domain Event)
- Raised when payment fails
- Translated to OrderPaymentEvent for Kafka

## Domain-Driven Design Architecture

### Aggregates

**Product Aggregate** (Inventory Microservice)
```
Product (Aggregate Root)
├── ProductId (Identity)
├── ProductSku (Value Object) - Unique identifier
├── ProductTitle (Value Object)
├── ProductDescription (Value Object)
├── Money (Value Object) - Price with VAT
├── Quantity (Value Object) - Stock quantity
├── ImageUrl (Value Object) - Product image
└── ProductCategory (Entity) - Many-to-many relationship

Business Rules:
- SKU must be unique across all products
- Price must be positive
- Volume cannot go negative
- Category assignment prevents duplicates
```

**Order Aggregate** (Order Microservice)
```
Order (Aggregate Root)
├── OrderId (Identity)
├── CustomerId (Value Object)
├── OrderStatus (Value Object) - CREATED, PAID, SHIPPED, DELIVERED, PAYMENT_FAILED
├── OrderItem (Entity) - Collection
│   ├── ProductId (Value Object)
│   ├── Quantity (Value Object)
│   └── Price (Money Value Object)
└── Total Amount (Money Value Object)

Business Rules:
- Status transitions are validated (CREATED → PAID → SHIPPED → DELIVERED)
- Cannot add items after order is PAID
- Quantity must be positive
- Total amount auto-calculated from items
```

**Payment Aggregate** (Payment Microservice)
```
Payment (Aggregate Root)
├── PaymentId (Identity)
├── OrderId (Value Object)
├── PaymentMethod (Value Object) - CREDIT_CARD, DEBIT_CARD, etc.
├── PaymentStatus (Value Object) - PENDING, SUCCESSFUL, FAILED, CANCELLED
├── Money (Value Object) - Amount
└── Transaction Details

Business Rules:
- Payment can only be processed once from PENDING
- Failed payments can be retried
- Successful payments cannot be modified
- Simulates 80% success rate for demo
```

### Layered Architecture

Each microservice follows clean architecture:

```
presentation/
├── controller/          # REST API controllers
└── dto/                 # Data Transfer Objects

application/
├── service/            # Application services (orchestration)
├── mapper/             # DTO ↔ Domain mapping
└── event/              # Event handlers & publishers

domain/
├── model/
│   ├── aggregate/      # Aggregate roots
│   ├── entity/         # Entities
│   └── valueobject/    # Value objects
├── service/            # Domain services
└── repository/         # Repository interfaces (ports)

infrastructure/
├── persistence/        # JPA implementations
│   ├── repository/     # Repository implementations
│   └── entity/         # JPA entities
├── messaging/          # Kafka producers/consumers
└── config/             # Configuration classes
```

### Shared Kernel

The `shared` module provides common domain building blocks:

**Base Classes:**
- `AggregateRoot<ID>` - Base for all aggregates
- `Entity<ID>` - Base for entities
- `ValueObject` - Base for value objects
- `DomainEvent` - Base for domain events
- `DomainObjectId` - Generic identifier

**Financial Domain:**
- `Money` - Monetary values with currency
- `VAT` - Value-added tax calculations

**Event Infrastructure:**
- `DomainEventPublisher` - Publishing interface
- `ProtobufDomainTranslator` - Domain events → Protobuf
- `DelegatingDomainEventTranslator` - Translator registry

## Kafka Infrastructure

### Broker Setup (docker-compose.yml)

```yaml
Services:
- Zookeeper (Port 2181)
- Kafka Broker (Ports 9092 internal, 9094 external)
- Schema Registry (Port 8081)
- Schema Registry UI (Port 8001)
- Kafka REST Proxy (Port 8082)
- Kafka Topics UI (Port 8000)
```

### Kafka Topics

| Topic Name | Publisher | Consumers | Event Type |
|------------|-----------|-----------|------------|
| `product-created` | Product MS | Product MS | ProductCreatedEvent |
| `product-updated` | Product MS | Product MS | ProductUpdatedEvent |
| `order-created-events` | Order MS | Payment MS | OrderCreatedEvent |
| `order-updated` | Order MS | - | OrderStatusChangedEvent |
| `order-payment-events` | Payment MS | Order MS | OrderPaymentEvent |

### Serialization

- **Format**: Protocol Buffers (Protobuf)
- **Serializer**: `KafkaProtobufSerializer`
- **Deserializer**: `KafkaProtobufDeserializer`
- **Schema Registry**: Confluent Schema Registry
- **Schema Evolution**: Backward and forward compatible

### Kafka Configuration

**Producer:**
```java
enable.idempotence=true
acks=all
key.serializer=StringSerializer
value.serializer=KafkaProtobufSerializer
```

**Consumer:**
```java
enable.auto.commit=false
isolation.level=read_committed
value.deserializer=ErrorHandlingDeserializer
spring.json.use.type.headers=false
```

## Database Configuration

### PostgreSQL Instances

**Product Database (bookstore)**
- Port: 5432
- Schema: bookstore
- Tables:
  - `product_table` - Product aggregate data
  - `categories` - Product categories
  - `product_category_map` - Product-category mapping

**Order Database (bookstore-order)**
- Port: 5433
- Schema: bookstore-order
- Tables:
  - `orders` - Order aggregate data
  - `order_items` - Order line items

**Payment Database**
- Managed by Flyway migrations
- Tables:
  - Payment transaction data

### ORM & Caching

- **ORM**: Hibernate/JPA
- **Connection Pool**: HikariCP
- **2nd Level Cache**: Hibernate + Caffeine
- **Migrations**: Flyway

## Communication Patterns

### Synchronous (REST)
- **Client ↔ Microservices**: HTTP/REST
- **Use Cases**: Read operations, immediate responses
- **Protocol**: JSON over HTTP

### Asynchronous (Events)
- **Microservice ↔ Microservice**: Kafka events
- **Use Cases**: State changes, long-running operations, saga coordination
- **Protocol**: Protobuf over Kafka
- **Pattern**: Event choreography (no orchestrator)

### No Direct Service Calls
- Microservices never call each other directly via HTTP
- All inter-service communication via Kafka events
- Enables loose coupling and independent scaling

## Data Consistency

### Eventual Consistency
- State propagated through events
- Services maintain their own data
- No distributed transactions

### Transactional Outbox
- Domain events published within transaction
- Ensures consistency between database writes and event publishing

### Idempotent Consumers
- Event handlers designed for at-least-once delivery
- Duplicate events handled gracefully

## Saga Pattern Implementation

### Order-Payment Choreography Saga

**Happy Path:**
1. User creates order → Order status: CREATED
2. OrderCreatedEvent published
3. Payment service consumes event → Processes payment
4. PaymentProcessedEvent published (80% chance)
5. Order service consumes event → Order status: PAID
6. Order can proceed to SHIPPED → DELIVERED

**Compensation Path:**
1. Payment fails (20% chance)
2. PaymentFailedEvent published
3. Order service consumes event → Order status: PAYMENT_FAILED
4. User notified to retry payment or cancel

### Saga Characteristics
- **Type**: Choreography-based (no central coordinator)
- **Compensation**: Automatic on payment failure
- **Idempotency**: All event handlers are idempotent
- **Audit Trail**: All events stored in Kafka for replay

## Resilience Patterns

### Retry Pattern
```java
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000, multiplier = 2.0)
)
```

### Transaction Management
```java
@Transactional
public void handleEvent() {
    // Database operations + event publishing
    // All-or-nothing semantics
}
```

### Error Handling
- `ErrorHandlingDeserializer` for Kafka
- Dead letter topics for failed messages
- Circuit breaker pattern (can be added)

## Testing Strategy

### Unit Tests
- Domain logic (aggregates, value objects, entities)
- Value object validations
- Business rule enforcement

### Integration Tests
- Repository tests with TestContainers
- Kafka event publishing/consuming
- REST API endpoints
- End-to-end order flow

### Test Fixtures
- Shared test utilities in `shared-test` module
- Test data builders
- In-memory repositories for fast tests

## Development Environment

### Local Setup
```bash
# Start infrastructure
docker-compose up -d

# Start microservices
./gradlew :inventory-microservice:bootRun    # Port 8083
./gradlew :order-microservice:bootRun        # Port 8080
./gradlew :payment-microservice:bootRun      # Port 8084

# Start frontend
cd frontend && npm start                      # Port 3000
```

### Docker Setup
```bash
# Build all services
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f <service-name>
```

## System Characteristics

### Scalability
- Each microservice can scale independently
- Kafka partitioning enables horizontal scaling
- Stateless services (except shopping cart in-memory)

### Fault Tolerance
- Kafka message persistence
- Retry mechanisms
- Graceful degradation on service failures

### Observability
- Structured logging
- Kafka message tracing
- Health check endpoints (Spring Boot Actuator)

## Future Enhancements

### Planned (Not Yet Implemented)
- API Gateway (Spring Cloud Gateway)
- Service Discovery (Eureka)
- Centralized Configuration (Spring Cloud Config)
- Redis for distributed caching and cart persistence
- Notification Service (email, SMS)
- User/Authentication Service (OAuth2/JWT)
- Monitoring Stack (Prometheus, Grafana, ELK, Jaeger)
- Shipping Service
- Inventory reservation on order creation

### Architecture Evolution
- Event sourcing for complete audit trail
- CQRS for read/write separation
- Service mesh for advanced traffic management
- GraphQL API gateway for flexible queries
