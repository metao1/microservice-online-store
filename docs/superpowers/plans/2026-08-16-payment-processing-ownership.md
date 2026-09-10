# Payment Processing Ownership Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow authenticated customers to process only payments owned by their JWT subject while allowing administrators and existing privileged callers to process any payment.

**Architecture:** Persist the authenticated creator subject as immutable payment ownership data and carry it through the aggregate, JPA entity, and internal application DTO. The HTTP adapter loads that owner before mutation, returns 403 for a customer-owner mismatch, and bypasses the ownership comparison for `ADMIN`, `STAFF`, and `payments:process` callers.

**Tech Stack:** Java 25, Spring Boot/Security method authorization, JPA/Hibernate, Flyway, PostgreSQL, JUnit 5, RestAssured, Testcontainers.

## Global Constraints

- Ownership comes only from the validated JWT `sub`; request bodies cannot select it.
- A cross-customer process attempt returns HTTP 403 before payment state changes.
- `ADMIN` can process any payment; retain existing `STAFF` and `SCOPE_payments:process` compatibility.
- Legacy rows have no recoverable owner and remain customer-inaccessible; privileged callers can still process them.
- Do not expose payment owner identifiers in JSON responses.

---

### Task 1: Persist Payment Ownership

**Files:**
- Create: `payment-microservice/src/main/resources/migration/V9__payment_add_user_id.sql`
- Modify: `payment-microservice/src/main/java/com/metao/book/payment/domain/model/aggregate/PaymentAggregate.java`
- Modify: `payment-microservice/src/main/java/com/metao/book/payment/domain/service/PaymentDomainService.java`
- Modify: `payment-microservice/src/main/java/com/metao/book/payment/application/service/PaymentApplicationService.java`
- Modify: `payment-microservice/src/main/java/com/metao/book/payment/application/dto/PaymentDTO.java`
- Modify: `payment-microservice/src/main/java/com/metao/book/payment/application/mapper/PaymentApplicationMapper.java`
- Modify: `payment-microservice/src/main/java/com/metao/book/payment/infrastructure/persistence/entity/PaymentEntity.java`
- Modify: `payment-microservice/src/main/java/com/metao/book/payment/infrastructure/persistence/mapper/PaymentEntityMapper.java`
- Create: `payment-microservice/src/test/java/com/metao/book/payment/infrastructure/persistence/mapper/PaymentEntityMapperTest.java`
- Test: `payment-microservice/src/test/java/com/metao/book/payment/application/service/PaymentAggregateApplicationServiceTest.java`

**Interfaces:**
- Consumes: `CreatePaymentCommand.userId(): String` populated from `CurrentUser.subject()`.
- Produces: `PaymentAggregate.getUserId(): String` and internal `PaymentDTO.userId(): String`, with the DTO field annotated `@JsonIgnore`.

- [ ] **Step 1: Write failing ownership persistence tests**

Add mapper and application-service assertions proving `customer-123` survives create, entity round-trip, and DTO mapping:

```java
assertThat(savedPayment.getUserId()).isEqualTo("customer-123");
assertThat(roundTrippedPayment.getUserId()).isEqualTo("customer-123");
assertThat(result.userId()).isEqualTo("customer-123");
```

- [ ] **Step 2: Run the focused tests and verify RED**

Run:

```bash
./gradlew :payment-microservice:test --tests '*PaymentEntityMapperTest' --tests '*PaymentAggregateApplicationServiceTest' --no-parallel
```

Expected: compilation or assertion failure because payment ownership is not stored.

- [ ] **Step 3: Add the minimal ownership model and migration**

Add a nullable migration so existing databases upgrade safely without inventing ownership:

```sql
ALTER TABLE payment ADD COLUMN user_id VARCHAR(255);
```

Add `userId` to the aggregate, entity, and mapper paths. New creation must call:

```java
paymentDomainService.createPayment(command.userId(), orderId, amount, paymentMethod);
```

Add the internal DTO field without exposing it:

```java
@JsonIgnore
String userId,
```

Keep existing aggregate constructors/reconstruction overloads as compatibility delegates where tests or internal event flows still need them. New HTTP-created payments must always persist the JWT-derived subject.

- [ ] **Step 4: Run focused ownership tests and verify GREEN**

Run the Step 2 command again. Expected: all selected tests pass.

- [ ] **Step 5: Commit ownership persistence**

```bash
git add payment-microservice/src/main payment-microservice/src/test
git commit -m "feat: persist payment ownership"
```

### Task 2: Enforce Customer Ownership Before Processing

**Files:**
- Modify: `payment-microservice/src/main/java/com/metao/book/payment/presentation/PaymentController.java`
- Modify: `payment-microservice/src/test/java/com/metao/book/payment/presentation/PaymentAggregateControllerIT.java`

**Interfaces:**
- Consumes: `PaymentUseCase.getPaymentById(String): Optional<PaymentDTO>`, `PaymentDTO.userId()`, `CurrentUser.subject()`, `CurrentUser.hasRole(String)`, and `CurrentUser.hasScope(String)`.
- Produces: customer-owned processing, HTTP 403 cross-owner rejection before mutation, and privileged processing bypass.

- [ ] **Step 1: Split test JWTs by subject and authority**

Configure decoder fixtures for:

```java
customerToken("customer-a", List.of("CUSTOMER"));
customerToken("customer-b", List.of("CUSTOMER"));
customerToken("admin", List.of("ADMIN"));
```

Create a pending payment as `customer-a`, then add tests asserting:

```java
// customer-a processes own payment -> 200
// customer-b processes customer-a payment -> 403
// persisted status remains PENDING after the rejected request
// admin processes customer-a payment -> 200
```

- [ ] **Step 2: Run the controller integration tests and verify RED**

Run:

```bash
./gradlew :payment-microservice:test --tests '*PaymentAggregateControllerIT' --no-parallel
```

Expected: own-customer processing returns 403 under the current method policy.

- [ ] **Step 3: Implement authorization before mutation**

Broaden the method gate to authenticated customer and existing privileged authorities:

```java
@PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('STAFF') or hasAuthority('SCOPE_payments:process')")
```

Before calling `processPayment`, load the payment and enforce ownership for non-privileged callers:

```java
PaymentDTO payment = paymentUseCase.getPaymentById(paymentId)
    .orElseThrow(() -> new PaymentNotFoundException(PaymentId.of(paymentId)));
boolean privileged = CurrentUser.hasRole("ADMIN")
    || CurrentUser.hasRole("STAFF")
    || CurrentUser.hasScope("payments:process");
if (!privileged && !Objects.equals(payment.userId(), CurrentUser.subject())) {
    throw new AccessDeniedException("Payment is owned by another customer");
}
return paymentUseCase.processPayment(paymentId);
```

The existing shared `SecurityExceptionHandler` maps `AccessDeniedException` to HTTP 403. Because the check runs before `processPayment`, rejected requests cannot mutate payment state.

- [ ] **Step 4: Run focused and module verification**

Run:

```bash
./gradlew :payment-microservice:test --tests '*PaymentAggregateControllerIT' --no-parallel
./gradlew :payment-microservice:test --no-parallel
```

Expected: focused authorization scenarios and the complete payment test suite pass.

- [ ] **Step 5: Update the OpenSpec task and commit**

Mark task `3.5` complete only after the full module suite succeeds, then run the available spec validation command if the OpenSpec CLI is installed.

```bash
git add payment-microservice openspec/changes/add-keycloak-pkce-authentication/tasks.md
git commit -m "fix: enforce payment processing ownership"
```

### Task 3: Runtime Verification

**Files:**
- No source changes expected.

**Interfaces:**
- Consumes: rebuilt `payment-microservice`, a fresh customer JWT, and a newly created payment owned by that subject.
- Produces: live evidence that CORS succeeds, own-customer processing succeeds, and cross-customer processing returns 403.

- [ ] **Step 1: Rebuild only the payment service**

```bash
docker compose up -d --build payment-microservice
```

- [ ] **Step 2: Verify readiness and CORS preflight**

```bash
curl -i http://localhost:8084/actuator/health
curl -i -X OPTIONS http://localhost:8084/payments/example/process \
  -H 'Origin: http://localhost:3000' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: authorization,content-type'
```

Expected: health is UP and preflight returns HTTP 200 with the configured CORS headers.

- [ ] **Step 3: Verify with newly created payment data**

Use fresh customer sessions and a payment created after V9 migration. Verify the owner receives HTTP 200 and a different customer receives HTTP 403. Do not log or persist bearer tokens.

Legacy rows created before V9 have no trustworthy owner. Do not backfill them automatically; use an administrator to process them or create new test data.
