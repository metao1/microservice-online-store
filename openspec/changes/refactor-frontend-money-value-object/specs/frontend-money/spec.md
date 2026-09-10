## ADDED Requirements

### Requirement: Frontend monetary values are cohesive
The frontend SHALL represent each domain monetary value as an immutable `Money` object containing its amount, currency, and monetary behavior. It SHALL retain distinct semantic properties such as price, subtotal, tax, total, and payment amount, while each such property holds a `Money` value.

#### Scenario: A product price is represented
- **WHEN** the frontend maps a product response containing an amount and currency
- **THEN** the product price is a `Money` value carrying both fields

#### Scenario: A non-monetary total is represented
- **WHEN** the frontend maps a pagination result containing a total record count
- **THEN** the record count remains a number and is not converted to `Money`

### Requirement: Money behavior is colocated with its contract
Each frontend `Money` value SHALL provide `format` and `multiply` operations declared by the `Money` interface and implemented by the colocated `createMoney` factory.

#### Scenario: A component formats money
- **WHEN** a component displays a monetary value
- **THEN** it calls the value's `format` operation and does not separately combine an amount with a currency symbol

#### Scenario: A quantity changes a line total
- **WHEN** a unit price is multiplied by a quantity
- **THEN** `multiply` returns a new `Money` value with the same currency and the calculated amount

### Requirement: API boundaries hydrate Money values
Frontend API adapters SHALL accept the backend monetary representations used by the services and hydrate them through `createMoney` before returning frontend domain models.

#### Scenario: Backend returns nested money
- **WHEN** an API response contains `{ "amount": 24.25, "currency": "EUR" }` as a monetary property
- **THEN** the adapter returns a hydrated `Money` value with working behavior

#### Scenario: Backend returns sibling amount and currency
- **WHEN** an API response contains a numeric payment amount and a sibling currency
- **THEN** the adapter combines them into one hydrated `Money` value
