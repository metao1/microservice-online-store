# Change: Complete Test Coverage for Inventory Microservice

## Why
The inventory-microservice currently has limited test coverage, with several critical components untested. This creates risk for regression bugs and makes refactoring difficult. Comprehensive test coverage is needed to ensure reliability, maintainability, and confidence in future changes.

## What Changes
- Add unit tests for ProductDomainService (business logic layer)
- Add unit tests for repository implementations (CategoryRepositoryImpl, ProductRepositoryImpl)
- Add unit tests for entity mappers (ProductEntityMapper, CategoryEntityMapper, ProductApplicationMapper)
- Add integration tests for ProductController REST endpoints
- Add tests for domain value objects (ImageUrl, ProductSku, CategoryName, etc.)
- Add tests for ProductAggregate edge cases and business rules
- Configure JaCoCo for code coverage reporting
- Set minimum coverage thresholds (80% line coverage, 70% branch coverage)

## Impact
- Affected specs: `inventory-microservice` (new spec)
- Affected code:
  - `inventory-microservice/src/test/` - new test classes
  - `inventory-microservice/build.gradle` - JaCoCo configuration
  - `.github/workflows/` - CI pipeline to enforce coverage thresholds
- No breaking changes to production code
- Improves code quality and maintainability
- Enables safer refactoring and feature additions
