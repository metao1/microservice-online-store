# External Product Seed Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Load generator seed products from a read-only Compose bind mount without packaging the dataset in the inventory JAR.

**Architecture:** `ProductGenerator` consumes a configurable Spring `Resource`, defaulting to the existing classpath location for tests and IDE runs. Compose overrides it with `file:/app/data/products.txt` and mounts the version-controlled dataset read-only.

**Tech Stack:** Java 25, Spring Boot 4.1, JUnit 5, Mockito, Docker Compose.

## Global Constraints

- Keep `bootJar { exclude("data/**") }` so the 10 MB dataset stays outside the image application JAR.
- Preserve existing unrelated Dockerfile and `ProductGenerator` edits.
- The generator remains active only for the existing `generator` profile.
- Missing configured seed data must fail generator-profile startup with a clear error.

---

### Task 1: Configurable External Product Seed

**Files:**
- Modify: `inventory-microservice/src/main/java/com/metao/book/product/infrastructure/factory/handler/ProductGenerator.java`
- Create: `inventory-microservice/src/test/java/com/metao/book/product/infrastructure/factory/handler/ProductGeneratorTest.java`
- Modify: `inventory-microservice/src/main/resources/application.yml`
- Modify: `docker-compose.yml`

**Interfaces:**
- Consumes: `product.seed.resource`, defaulting to `classpath:data/products.txt`.
- Produces: Compose value `PRODUCT_DATA_RESOURCE=file:/app/data/products.txt` and read-only bind mount `/app/data/products.txt`.

- [ ] **Step 1: Write failing resource tests**

Create focused tests that construct `ProductGenerator` with a readable `FileSystemResource` and a missing resource. Assert the readable resource passes validation and the missing resource throws `IllegalStateException` containing `Product seed resource is not readable`.

- [ ] **Step 2: Run the focused tests and confirm RED**

Run:

```bash
./gradlew :inventory-microservice:test \
  --tests com.metao.book.product.infrastructure.factory.handler.ProductGeneratorTest \
  --no-daemon --no-parallel
```

Expected: compilation fails because the resource constructor and validation method do not exist.

- [ ] **Step 3: Implement configurable resource validation**

Replace field injection with constructor injection equivalent to:

```java
public ProductGenerator(
    ProductUseCase productUseCase,
    ObjectMapper dtoMapper,
    @Value("${product.seed.resource:classpath:data/products.txt}") Resource resource
) {
    this.productUseCase = productUseCase;
    this.dtoMapper = dtoMapper;
    this.resource = resource;
}

@PostConstruct
void validateSeedResource() {
    if (!resource.exists() || !resource.isReadable()) {
        throw new IllegalStateException("Product seed resource is not readable: " + resource.getDescription());
    }
}
```

Retain the existing `generator` profile condition and asynchronous import behavior.

- [ ] **Step 4: Configure application and Compose**

Add to `application.yml` under `product`:

```yaml
seed:
  resource: ${PRODUCT_DATA_RESOURCE:classpath:data/products.txt}
```

Add to the inventory service:

```yaml
environment:
  PRODUCT_DATA_RESOURCE: file:/app/data/products.txt
volumes:
  - type: bind
    source: ./inventory-microservice/src/main/resources/data/products.txt
    target: /app/data/products.txt
    read_only: true
```

- [ ] **Step 5: Verify unit and configuration behavior**

Run:

```bash
./gradlew :inventory-microservice:test \
  --tests com.metao.book.product.infrastructure.factory.handler.ProductGeneratorTest \
  --no-daemon --no-parallel
docker compose config --quiet
```

Expected: focused tests and Compose validation pass.

- [ ] **Step 6: Verify the rebuilt container**

Run:

```bash
docker compose up -d --build --force-recreate inventory-microservice
docker compose logs --since=2m inventory-microservice
```

Expected: no `FileNotFoundException` for `data/products.txt`; generator logs that it parsed products.

- [ ] **Step 7: Commit**

Stage only the four scoped files and commit:

```bash
git commit -m "fix: load product seed from external resource"
```
