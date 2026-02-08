# Contributing to Microservice Online Store

## Testing Standards

### Inventory Microservice Test Coverage Plan

We have a comprehensive test coverage improvement plan located at:
📄 [plan-strengthenInventoryMicroserviceTestCoverage.prompt.md](../plan-strengthenInventoryMicroserviceTestCoverage.prompt.md)

**Current Focus:** Strengthening unit and integration tests for the inventory-microservice.

### Implementation Phases

The plan is organized into three phases:

#### **Phase 1: High Priority** (Start Here)
1. `ProductTest.java` - Core aggregate logic tests
2. Enhance `ProductManagementIT.java` - Integration scenarios

#### **Phase 2: Medium Priority**
3. `ProductCategoryTest.java` - Entity behavior tests
4. `ProductVolumeTest.java` - Value object edge cases

#### **Phase 3: Nice to Have**
5. `CategoryNameTest.java` - Value object validation
6. `ProductMoneyTest.java` - Financial operations

### Before You Write Tests

1. **Read the plan** - Understand the 10 test gaps identified
2. **Review Further Considerations:**
   - Test Organization: Use layer-based structure (`domain/model/**`, `infrastructure/application/**`)
   - Edge Cases: Focus on business rule violations, not just null checks
   - Mock vs. Real: Unit tests mock, IT tests use real dependencies
3. **Check existing tests** - See ProductApplicationServiceTest.java for patterns

### Writing Tests

#### Test Organization
```
src/test/java/com/metao/book/productAggregate/
├── domain/model/aggregate/ProductTest.java
├── domain/model/entity/ProductCategoryTest.java
├── domain/model/valueobject/CategoryNameTest.java
└── infrastructure/application/ProductManagementIT.java
```

#### Test Naming Pattern
```java
@Test
void methodName_whenCondition_shouldExpectedResult()
```

#### Example Test Structure
```java
@Test
void updatePrice_whenPriceChanges_shouldRaiseProductUpdatedEvent() {
    // GIVEN
    Product productAggregate = createTestProduct();
    Money newPrice = Money.of(BigDecimal.valueOf(25.00), Currency.getInstance("EUR"));
    
    // WHEN
    productAggregate.updatePrice(newPrice);
    
    // THEN
    assertThat(productAggregate.getMoney()).isEqualTo(newPrice);
    assertThat(productAggregate.getUpdatedTime()).isNotNull();
    // Verify domain event
    assertThat(productAggregate.getDomainEvents()).hasSize(2); // Created + Updated
}
```

#### AssertJ Fluent API
Always use AssertJ for assertions:
```java
assertThat(productAggregate)
    .isNotNull()
    .hasFieldOrPropertyWithValue("title", expectedTitle);

assertThat(categories)
    .isNotEmpty()
    .hasSize(1)
    .extracting(ProductCategory::getName)
    .containsExactly(expectedCategory);
```

### Test Coverage Goals

Based on the plan's Success Criteria:
- ✓ All domain model behaviors tested with unit tests
- ✓ IT tests cover happy path, error cases, and edge cases
- ✓ Test coverage improves from ~30% to ~80%+
- ✓ All tests pass with green indicators
- ✓ Domain events are verified in tests

### Test Gaps to Address

Check the plan for the complete list of 10 gaps:
1. ✗ No unit tests for Product aggregate domain logic
2. ✗ No tests for ProductCategory entity
3. ✗ No tests for value objects
4. ✗ No IT tests for error scenarios
5. ✗ No IT tests for GET endpoints
6. ✗ No validation/constraint tests
7. ✗ No domain event verification
8. ✗ Volume reduction edge cases not tested
9. ✗ Price update rules not tested
10. ✗ Category addition logic not thoroughly tested

### Creating a Test PR

Use the PR template: `.github/pull_request_template.md`

Reference the plan in your PR description:
```
Implements Phase 1, Task 1: ProductTest.java

Related: plan-strengthenInventoryMicroserviceTestCoverage.prompt.md
Addresses gaps: #1, #7, #8
```

### Running Tests Locally

```bash
# Run all inventory-microservice tests
./gradlew inventory-microservice:test

# Run a specific test class
./gradlew inventory-microservice:test --tests ProductTest

# Run with coverage report
./gradlew inventory-microservice:jacocoTestReport
```

### Questions?

1. Check the plan file for detailed context
2. Review existing test patterns in ProductApplicationServiceTest.java
3. Reference the Further Considerations section for design decisions

---

**Last Updated:** Based on plan-strengthenInventoryMicroserviceTestCoverage.prompt.md

