## ADDED Requirements

### Requirement: Order use cases are explicit inbound ports
Order controllers and listeners SHALL call explicit inbound use-case interfaces for creating orders, updating order status, fetching customer orders, and performing order item or inventory-related operations.

#### Scenario: REST creates an order
- **GIVEN** a client submits an order creation request
- **WHEN** the order controller handles the request
- **THEN** it SHALL map presentation DTOs to an application command and call `CreateOrderUseCase`

#### Scenario: Kafka payment event updates an order
- **GIVEN** a valid payment event arrives
- **WHEN** the payment listener maps it to an application command
- **THEN** it SHALL call `HandleOrderPaymentEventUseCase` as an interface

### Requirement: Cart use cases hide persistence and transport concerns
Cart behavior SHALL be exposed through application use-case interfaces and outbound ports, with JPA entities in infrastructure and HTTP DTOs in presentation.

#### Scenario: Cart quantity is set to zero
- **GIVEN** a client requests a zero quantity
- **WHEN** the controller validates the request
- **THEN** the system SHALL route it to an explicit remove-item command or reject it according to documented API behavior, and SHALL NOT return `null` from the application service

#### Scenario: Order payment clears a cart
- **GIVEN** an order reaches a paid state
- **WHEN** cart clearing is required
- **THEN** the action SHALL occur through a cart use case or port with an explicit local-transaction or eventual-event decision

### Requirement: Payment processing uses a gateway port
Payment application services SHALL charge payments through a `PaymentGatewayPort`, then apply successful, failed, pending, cancelled, or retryable results to the payment aggregate.

#### Scenario: Development fake gateway succeeds
- **GIVEN** the development fake payment gateway adapter is active
- **WHEN** the payment application service charges an order
- **THEN** the fake adapter MAY return success, but the payment aggregate SHALL NOT hardcode simulated success internally

### Requirement: Order and payment statuses transition explicitly
Order and payment aggregates SHALL define complete transition matrices for success, failure, cancellation, and retry behavior, using typed statuses across application and domain layers.

#### Scenario: Payment fails for a created order
- **GIVEN** an order is in `CREATED` or the chosen pre-payment state
- **WHEN** a failed payment event is handled
- **THEN** the order SHALL transition to the configured failed-payment state without violating domain transition rules

#### Scenario: Invalid status string is submitted
- **GIVEN** a client submits an invalid status value
- **WHEN** the presentation adapter parses the request
- **THEN** validation SHALL fail before application or domain services receive the command

### Requirement: Payment event translators are exhaustive
Payment integration-event translators SHALL map each supported payment status explicitly and SHALL NOT default unknown, pending, or cancelled states to failed.

#### Scenario: Cancelled payment is translated
- **GIVEN** a payment cancellation is emitted
- **WHEN** the translator creates an integration event
- **THEN** it SHALL produce the documented cancellation event or status mapping, not a generic failure by default

### Requirement: Orders persist financial snapshots
Orders SHALL persist the VAT rate, subtotal, tax, total, and currency/tax policy data needed to rehydrate historical orders deterministically.

#### Scenario: VAT configuration changes after order creation
- **GIVEN** an order was created with one VAT rate
- **WHEN** runtime VAT configuration changes and the order is read later
- **THEN** the order totals SHALL match the persisted creation-time financial snapshot
