# Change: Refactor frontend structure for maintainability

## Why
The frontend has grown complex across Navigation, Products, Search, layout, and UI components, which makes changes risky and slow. A structural refactor will reduce coupling and improve readability while preserving existing behavior.

## What Changes
- Reorganize frontend components into clearer domains (navigation, products, search, layout, shared UI).
- Extract large components into smaller, focused modules.
- Normalize shared utilities and types across UI components.
- Preserve current UI, interactions, and API contracts (no behavior changes).

## Impact
- Affected code: `frontend/src/components`, `frontend/src/pages`, `frontend/src/context`, `frontend/src/hooks`, `frontend/src/services`.
- Affected capabilities: frontend structure and UI component organization.
- **Non-breaking**: functionality and visual behavior must remain the same.
