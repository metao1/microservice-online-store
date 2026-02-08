## Context
The frontend has accumulated complex, large files with tightly coupled UI, state, and layout logic. This refactor focuses on structural organization without altering behavior or visuals.

## Goals / Non-Goals
- Goals: Reduce component size, clarify ownership of state, improve reusability, and simplify imports.
- Non-Goals: UI redesigns, changing API payloads, or modifying product/search behavior.

## Decisions
- Decision: Introduce domain folders (navigation, products, search, layout, ui) and shared utilities.
- Decision: Split large components into presentational + container logic where appropriate.

## Risks / Trade-offs
- Risk: Regressions due to moving files and imports.
  - Mitigation: Keep component APIs stable and run build + smoke checks.

## Migration Plan
- Move components incrementally, updating imports per step.
- Keep UI snapshots/behavior stable with manual smoke checks.

## Open Questions
- None.
