## 1. Money value object
- [x] 1.1 Add the immutable `Money` interface and colocated `createMoney` factory.
- [x] 1.2 Add tests for construction, validation, formatting, multiplication, and immutability.

## 2. API normalization
- [x] 2.1 Normalize nested and sibling backend money shapes at API boundaries.
- [x] 2.2 Update remote and mock API clients to hydrate `Money` values.

## 3. Frontend model migration
- [x] 3.1 Migrate product prices and cart totals to `Money`.
- [x] 3.2 Migrate checkout and payment amounts to `Money`.
- [x] 3.3 Migrate order totals and line-item prices to `Money`.
- [x] 3.4 Update components and hooks to use value-object operations.
- [x] 3.5 Remove obsolete monetary helpers and duplicated currency fields.

## 4. Verification
- [x] 4.1 Run focused money, cart, checkout, order, and payment tests.
- [x] 4.2 Run the complete frontend test suite.
- [x] 4.3 Run the frontend production build.
