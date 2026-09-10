# Change: Introduce a frontend Money value object

## Why
Frontend models currently carry monetary amounts and currencies in separate fields and use several ad hoc conversion and formatting paths. This permits invalid combinations and caused order totals to display with the wrong value or currency.

## What Changes
- Add an immutable TypeScript `Money` interface and colocated `createMoney` factory.
- Put monetary behavior such as `format` and `multiply` on each `Money` value.
- Represent product prices, cart totals, order totals, line prices, payment amounts, subtotals, and taxes as `Money` values.
- Normalize existing backend response shapes into `Money` values at API boundaries.
- Update frontend components, hooks, mocks, and tests to use the common model.
- Keep pagination totals, quantities, tax percentages, and other non-monetary numbers unchanged.

## Impact
- Affected specs: `frontend-money`
- Affected code: `frontend/src/types`, API clients, cart/checkout/order/payment flows, UI formatting, mocks, and tests
- Backend HTTP contracts remain unchanged.
