# Test Coverage Design

## Context
The inventory-microservice currently has limited test coverage, with only 5 test classes covering basic functionality. Recent refactoring (Product → ProductAggregate rename) and bug fixes (category detached entity issue, image URL regex) highlighted the need for comprehensive testing to prevent regressions.

### Current State
- **Existing tests:**
  - `ProductAggregateTest.java` - Basic aggregate tests
  - `ProductCategoryEntityTest.java` - Basic category entity tests
  - `ProductApplicationServiceTest.java` - Service layer tests
  - `ProductCategoriesServiceTest.java` - Category service tests
  - `ProductManagementIT.java` - Integration tests (12 scenarios)

- **Missing coverage:**
  - ProductDomainService (complex business logic)
  - Repository implementations (persistence layer)
  - Entity mappers (critical for Hibernate session management)
  - Value objects (validation logic)
  - Controller layer (beyond basic integration tests)
  - Edge cases and error scenarios

### Constraints
- Must not slow down build significantly (< 30 second test execution)
- Must integrate with existing Spring Boot test infrastructure
- Must work with TestContainers for database integration tests
- Must support parallel test execution for CI efficiency

## Goals / Non-Goals

### Goals
- Achieve 80% line coverage and 70% branch coverage minimum
- Test all business logic and domain rules
- Test critical persistence layer edge cases (natural ID lookup, detached entities)
- Provide fast feedback via unit tests and comprehensive validation via integration tests
- Make test coverage visible and enforceable in CI pipeline

### Non-Goals
- 100% coverage (diminishing returns on simple getters/setters)
- Performance/load testing (separate concern)
- Security penetration testing (separate concern)
- End-to-end UI testing (frontend responsibility)

## Decisions

### Decision 1: Three-Layer Test Strategy
**What:** Organize tests into three distinct layers - unit, integration, and acceptance

**Why:**
- Unit tests: Fast feedback, test business logic in isolation
- Integration tests: Verify layer interactions, database, and Spring context
- Acceptance tests: E2E scenarios matching user stories

**Alternatives considered:**
- Single-layer integration-only tests: Too slow for TDD workflow
- Only unit tests: Misses critical Hibernate/JPA edge cases
- **Chosen:** Layered approach balances speed and confidence

**Implementation:**
```
src/test/java/
├── unit/               # Fast, isolated tests with mocks
│   ├── domain/
│   ├── application/
│   └── infrastructure/
├── integration/        # Spring context + TestContainers
│   └── repository/
└── acceptance/         # Full API integration tests
    └── api/
```

### Decision 2: JaCoCo for Coverage Measurement
**What:** Use JaCoCo Gradle plugin with build-time verification

**Why:**
- Industry standard for Java coverage
- Gradle integration is straightforward
- Supports exclusion patterns for generated code
- Generates HTML reports and XML for CI integration

**Alternatives considered:**
- Cobertura: Less actively maintained
- IntelliJ coverage: IDE-specific, not CI-friendly
- **Chosen:** JaCoCo for ecosystem compatibility

**Configuration:**
```gradle
jacoco {
    toolVersion = "0.8.11"
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80  // 80% line coverage
            }
        }
        rule {
            limit {
                counter = 'BRANCH'
                minimum = 0.70  // 70% branch coverage
            }
        }
    }
}
```

### Decision 3: Test Data Builders Pattern
**What:** Create builder classes for complex test fixtures

**Why:**
- Reduces test boilerplate
- Ensures valid default values
- Makes tests more readable
- Centralizes test data creation logic

**Alternatives considered:**
- Object Mother pattern: Less flexible
- Direct constructors: Too verbose
- Fixtures from JSON files: Harder to customize per test
- **Chosen:** Builders for flexibility and readability

**Example:**
```java
@Component
public class ProductTestBuilder {
    public ProductAggregate validProduct() {
        return new ProductAggregate(
            ProductSku.of("TEST000001"),
            ProductTitle.of("Test Product"),
            ProductDescription.of("Test Description"),
            ProductVolume.of(BigDecimal.valueOf(100)),
            new Money("EUR", 29.99),
            Instant.now(),
            Instant.now(),
            ImageUrl.of("https://example.com/test.jpg"),
            Set.of(CategoryTestBuilder.defaultCategory())
        );
    }

    public ProductTestBuilder withSku(String sku) { /* ... */ }
    public ProductTestBuilder withCategories(Set<ProductCategory> categories) { /* ... */ }
}
```

### Decision 4: Testcontainers for Database Tests
**What:** Use existing Testcontainers setup for integration tests requiring real database

**Why:**
- Already configured in project
- Ensures tests run against real PostgreSQL
- Catches database-specific issues (constraints, transactions)
- Isolated test execution

**Implementation:** Extend existing `KafkaContainer` base class pattern

### Decision 5: Natural ID Cache Testing Strategy
**What:** Explicitly test Hibernate natural ID lookups and session management

**Why:**
- Recent bug with detached CategoryEntity highlighted this gap
- Natural ID cache behavior is critical for performance
- Session management issues can cause subtle bugs

**Key scenarios:**
- Same category used by multiple products in one transaction
- Same category used by products in different transactions
- Natural ID cache hits vs misses
- @PrePersist ID generation timing

## Risks / Trade-offs

### Risk 1: Test Execution Time
**Risk:** Comprehensive test suite may slow down development feedback loop

**Mitigation:**
- Separate fast unit tests from slower integration tests
- Run unit tests by default, integration tests on CI only
- Use Gradle test filtering: `./gradlew test --tests '*Unit*'`
- Enable parallel test execution in Gradle

### Risk 2: Flaky Integration Tests
**Risk:** Database/container tests may have timing issues or resource conflicts

**Mitigation:**
- Use Testcontainers' automatic cleanup
- Ensure test isolation with @Transactional rollback
- Add retry logic for container startup
- Use unique SKUs/category names per test

### Risk 3: Coverage Threshold Too Strict
**Risk:** 80%/70% thresholds may be too high initially, blocking development

**Mitigation:**
- Start with lower thresholds (60%/50%) and gradually increase
- Allow exclusions for generated code and DTOs
- Package-level thresholds instead of global
- Review coverage in PR reviews, not just automated checks

### Risk 4: Test Maintenance Burden
**Risk:** Large test suite requires ongoing maintenance as code evolves

**Mitigation:**
- Use test data builders to centralize test data changes
- Write tests at the right level (prefer unit over integration when possible)
- Refactor tests when they become brittle
- Keep tests simple and focused on one scenario each

## Migration Plan

### Phase 1: Foundation (Tasks 1, 4)
1. Configure JaCoCo plugin in build.gradle
2. Create test data builder utilities
3. Set initial coverage thresholds at 60%/50%
4. Validate on CI without failing builds

**Milestone:** Test infrastructure ready, coverage visible

### Phase 2: Domain & Application Layer (Tasks 2, 3)
1. Add ProductDomainServiceTest
2. Expand ProductAggregateTest with edge cases
3. Add value object tests
4. Add mapper tests

**Milestone:** Business logic fully tested, coverage at ~70%

### Phase 3: Infrastructure Layer (Task 4)
1. Add repository implementation tests
2. Add entity mapper tests with natural ID scenarios
3. Test Hibernate session management edge cases

**Milestone:** Persistence layer fully tested, coverage at ~75%

### Phase 4: Presentation & Integration (Tasks 5, 6)
1. Expand controller tests
2. Add E2E lifecycle tests
3. Add concurrent access tests
4. Add cache interaction tests

**Milestone:** Full stack tested, coverage at 80%+

### Phase 5: Documentation & Enforcement (Task 7)
1. Generate and publish coverage reports
2. Update project documentation
3. Increase thresholds to 80%/70%
4. Enforce in CI pipeline

**Milestone:** Coverage enforced, documented, and maintained

## Open Questions

1. **Q:** Should we test the presentation layer DTOs (CreateProductCommand, UpdateProductCommand)?
   **A:** Yes, but focus on validation rules and mapping to domain objects. Skip simple getters.

2. **Q:** How to handle testing of Hibernate caching behavior?
   **A:** Use Hibernate statistics API in tests to verify cache hits/misses.

3. **Q:** Should we add mutation testing (PIT)?
   **A:** Defer to future enhancement. Focus on line/branch coverage first.

4. **Q:** How to test @PrePersist hooks in CategoryEntity?
   **A:** Integration test that verifies ID generated before persist, unit test with EntityManager mock.
