## Context
Backend services expose monetary data in more than one JSON shape: some values are `{ amount, currency }`, while others use sibling amount and currency properties. The frontend currently flattens these values into numbers and repeats currency separately.

## Goals / Non-Goals
- Goals: make amount and currency inseparable in frontend domain models; colocate monetary operations with the value; normalize transport differences once at API boundaries.
- Non-goals: change backend contracts, introduce global state management, implement exchange-rate conversion, or replace non-monetary numeric totals.

## Decisions
- Define an immutable `Money` interface with readonly `amount` and `currency`, plus `format(locale?)` and `multiply(multiplier)` methods.
- Implement the interface with a colocated `createMoney(amount, currency)` factory rather than a class.
- Convert JSON DTOs into `Money` values in real and mock API adapters because JSON cannot carry methods.
- Return a new `Money` value from arithmetic operations and reject non-finite amounts or multipliers.
- Preserve semantic property names: for example, `product.price`, `order.total`, and `payment.amount` each hold a `Money` value.

## Risks / Trade-offs
- Object spreading or JSON serialization removes methods. API and persistence boundaries must explicitly hydrate values through `createMoney`.
- This is a broad compile-time change. Migrate one model flow at a time and keep TypeScript compilation green after each step.

## Migration Plan
1. Add and test the `Money` contract and factory.
2. Centralize backend-money normalization.
3. Migrate products and cart.
4. Migrate checkout, orders, and payments.
5. Remove obsolete amount/currency helpers and run the frontend suite and production build.
