# Inventory Microservice Test Coverage Specification

## ADDED Requirements

### Requirement: Unit Test Coverage
The inventory-microservice SHALL have comprehensive unit tests for all business logic, service, and mapping layers with minimum 80% line coverage and 70% branch coverage.

#### Scenario: ProductDomainService business rules tested
- **GIVEN** ProductDomainService with category assignment logic
- **WHEN** tests execute for category limits, related products, and product uniqueness
- **THEN** all business rules are verified (max 5 categories, related product algorithm, SKU uniqueness)

#### Scenario: Repository implementations tested
- **GIVEN** CategoryRepositoryImpl and ProductRepositoryImpl
- **WHEN** tests execute CRUD operations, searches, and pagination
- **THEN** all database interactions work correctly with proper entity mapping

#### Scenario: Entity mappers tested
- **GIVEN** ProductEntityMapper and CategoryEntityMapper
- **WHEN** converting between domain and entity objects
- **THEN** all fields map correctly, natural ID lookups work, and session management is proper

#### Scenario: Value objects validated
- **GIVEN** ImageUrl, ProductSku, CategoryName, and other value objects
- **WHEN** tests validate constraints and edge cases
- **THEN** regex patterns work (URLs with hyphens), length constraints enforce, and invalid inputs reject

### Requirement: Integration Test Coverage
The inventory-microservice SHALL have integration tests for all REST endpoints, database interactions, and cross-layer scenarios.

#### Scenario: Controller endpoints tested
- **GIVEN** ProductController REST API
- **WHEN** integration tests execute for all endpoints
- **THEN** CRUD operations work, validation errors return proper HTTP status codes, and response DTOs match schema

#### Scenario: End-to-end product lifecycle tested
- **GIVEN** complete product workflow
- **WHEN** creating, updating, searching, and managing products
- **THEN** all operations persist correctly, transactions work, and database constraints enforce

#### Scenario: Concurrent access tested
- **GIVEN** multiple requests to same product
- **WHEN** concurrent updates occur
- **THEN** optimistic locking prevents lost updates and proper errors return

#### Scenario: Category natural ID lookup tested
- **GIVEN** multiple products with same category
- **WHEN** creating products in sequence or parallel
- **THEN** Hibernate session management works, natural ID cache hits, and no detached entity errors occur

### Requirement: Code Coverage Reporting
The inventory-microservice SHALL have automated code coverage reporting with enforced minimum thresholds.

#### Scenario: JaCoCo configured
- **GIVEN** Gradle build configuration
- **WHEN** running tests with coverage
- **THEN** JaCoCo generates HTML reports with line and branch coverage metrics

#### Scenario: Coverage thresholds enforced
- **GIVEN** JaCoCo verification rules
- **WHEN** coverage falls below thresholds (80% line, 70% branch)
- **THEN** build fails with clear error message indicating which packages need more tests

#### Scenario: CI pipeline checks coverage
- **GIVEN** GitHub Actions workflow
- **WHEN** pull request submitted
- **THEN** test coverage report generated and published, coverage badge updated, and PR fails if below threshold

### Requirement: Test Data Builders
The inventory-microservice SHALL have reusable test data builders for creating test fixtures.

#### Scenario: Product test builder
- **GIVEN** ProductTestBuilder utility class
- **WHEN** creating test products
- **THEN** valid products created with sensible defaults, builder pattern allows customization, and common scenarios have factory methods

#### Scenario: Category test builder
- **GIVEN** CategoryTestBuilder utility class
- **WHEN** creating test categories
- **THEN** unique category names generated, natural ID conflicts avoided, and session-attached entities provided

### Requirement: Test Documentation
The inventory-microservice SHALL have clear documentation for testing patterns, conventions, and how to run tests.

#### Scenario: Testing guide in README
- **GIVEN** project README
- **WHEN** developer wants to run tests
- **THEN** README explains test commands, coverage reports location, and minimum coverage requirements

#### Scenario: Test patterns documented
- **GIVEN** project.md conventions
- **WHEN** writing new tests
- **THEN** documentation shows patterns for unit tests, integration tests, test data creation, and mocking strategies

### Requirement: Edge Case Coverage
The inventory-microservice SHALL have tests for all edge cases and error conditions.

#### Scenario: Invalid image URLs rejected
- **GIVEN** product creation with various image URL formats
- **WHEN** URLs contain invalid patterns (missing protocol, wrong extension, invalid characters)
- **THEN** validation errors occur with clear messages

#### Scenario: Product volume constraints tested
- **GIVEN** product with volume operations
- **WHEN** reducing volume below zero, increasing beyond max, or invalid quantities
- **THEN** proper domain exceptions thrown and state remains consistent

#### Scenario: Category detached entity handling tested
- **GIVEN** multiple products sharing categories
- **WHEN** creating products in same or different transactions
- **THEN** Hibernate natural ID lookup works, no detached entity errors, and database constraints maintained

#### Scenario: Null and empty input handling tested
- **GIVEN** service methods with null or empty parameters
- **WHEN** calling with invalid inputs
- **THEN** appropriate exceptions thrown (NullPointerException, IllegalArgumentException) with descriptive messages
