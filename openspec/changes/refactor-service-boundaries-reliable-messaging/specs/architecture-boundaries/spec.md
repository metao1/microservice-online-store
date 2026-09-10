## ADDED Requirements

### Requirement: Layered dependency direction
The system SHALL keep presentation and infrastructure adapters outside the application layer, application use cases outside the domain layer, and framework-specific implementations outside domain packages.

#### Scenario: Domain package dependencies are checked
- **GIVEN** the architecture test suite runs
- **WHEN** classes under `*.domain.*` are inspected
- **THEN** they SHALL NOT import Spring, Jakarta Persistence, Apache Kafka, Jackson, HTTP, presentation packages, or infrastructure packages

#### Scenario: Adapters call application interfaces
- **GIVEN** a REST controller or Kafka listener invokes business behavior
- **WHEN** its dependencies are inspected
- **THEN** it SHALL depend on an application use-case interface rather than a concrete application service or domain service

### Requirement: Domain services are pure
The system SHALL keep domain services as ordinary business-rule collaborators without transaction annotations, component annotations, repositories, external clients, or application-service calls.

#### Scenario: Order orchestration creates an order
- **GIVEN** a request to create an order
- **WHEN** cart reads, aggregate creation, persistence, and event enqueueing are coordinated
- **THEN** that orchestration SHALL occur in an application service implementing an inbound use-case interface

### Requirement: Application services use outbound ports
Application services SHALL depend on ports or interfaces for persistence, messaging, cart access, payment gateways, and concurrency controls.

#### Scenario: An application service publishes domain events
- **GIVEN** an aggregate records domain events
- **WHEN** an application service persists the aggregate
- **THEN** the service SHALL use an event-publishing/outbox port rather than a concrete Kafka publisher

### Requirement: Persistence and REST models are isolated
The system SHALL place JPA entities and Spring Data repositories only under infrastructure persistence packages, and REST DTOs only under presentation packages.

#### Scenario: Cart item quantity is updated by HTTP
- **GIVEN** a cart update request is accepted by a controller
- **WHEN** the response is returned
- **THEN** the response SHALL be a presentation DTO and SHALL NOT expose a JPA entity, domain aggregate, or application persistence model

### Requirement: Shared domain remains technology-neutral
Shared domain types SHALL contain only stable business abstractions and value objects, without Spring, JPA, Kafka, Jackson, HTTP, or protobuf translator implementations.

#### Scenario: A payment entity is persisted
- **GIVEN** a payment aggregate is saved
- **WHEN** infrastructure maps it to persistence
- **THEN** the JPA entity SHALL be infrastructure-local and SHALL NOT extend a shared domain base class carrying framework annotations
