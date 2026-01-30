# Implementation Tasks

## 1. Test Infrastructure Setup
- [ ] 1.1 Add JaCoCo plugin to inventory-microservice/build.gradle
- [ ] 1.2 Configure JaCoCo with 80% line coverage and 70% branch coverage thresholds
- [ ] 1.3 Add test coverage reports to CI pipeline
- [ ] 1.4 Create test utility classes for common test data builders

## 2. Domain Layer Tests
- [ ] 2.1 Add ProductDomainServiceTest (business rules, category limits, related products)
- [ ] 2.2 Add comprehensive ProductAggregateTest edge cases (volume reduction, price updates, category management)
- [ ] 2.3 Add ProductCategoryTest (equality, validation)
- [ ] 2.4 Add value object tests (ImageUrl validation, ProductSku format, CategoryName constraints)

## 3. Application Layer Tests
- [ ] 3.1 Expand ProductApplicationServiceTest (create product scenarios, update scenarios, error cases)
- [ ] 3.2 Add ProductApplicationMapperTest (DTO ↔ Domain mapping, null handling)
- [ ] 3.3 Add comprehensive ProductCategoriesServiceTest

## 4. Infrastructure Layer Tests
- [ ] 4.1 Add CategoryRepositoryImplTest (findByName, save, existsByName)
- [ ] 4.2 Add ProductRepositoryImplTest (CRUD operations, search, pagination)
- [ ] 4.3 Add ProductEntityMapperTest (domain ↔ entity mapping, category handling)
- [ ] 4.4 Add CategoryEntityMapperTest (natural ID lookup, session management)

## 5. Presentation Layer Tests
- [ ] 5.1 Expand ProductManagementIT with more controller endpoint scenarios
- [ ] 5.2 Add ProductControllerTest (unit tests with mocked services)
- [ ] 5.3 Add validation tests for CreateProductCommand and UpdateProductCommand
- [ ] 5.4 Add error handling tests (404, 400, 500 scenarios)

## 6. Integration Tests
- [ ] 6.1 Add end-to-end product lifecycle tests (create → update → search → delete)
- [ ] 6.2 Add concurrent access tests (optimistic locking)
- [ ] 6.3 Add database constraint tests (unique SKU, category natural ID)
- [ ] 6.4 Add cache interaction tests (Hibernate 2nd-level cache)

## 7. Documentation and Reporting
- [ ] 7.1 Generate JaCoCo HTML coverage report
- [ ] 7.2 Document testing patterns and conventions in project.md
- [ ] 7.3 Add README section on running tests and coverage reports
- [ ] 7.4 Create test coverage badge for project README
