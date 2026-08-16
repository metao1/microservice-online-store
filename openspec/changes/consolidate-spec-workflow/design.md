## Context

OpenSpec and Superpowers currently both produce repository documents describing designs and implementation plans. In this project, `AGENTS.md` already makes OpenSpec authoritative, so the duplicate Superpowers artifacts add ambiguity without adding a separate contract.

## Decisions

- OpenSpec owns proposed and current behavioral requirements, architecture decisions, acceptance scenarios, approval state, and task tracking.
- OpenAPI owns machine-readable HTTP request and response contracts.
- Superpowers guides how approved work is investigated, implemented, tested, reviewed, and verified; it does not create a second repository specification hierarchy when OpenSpec applies.
- Bug fixes that restore already-specified behavior follow OpenSpec's direct-fix exception and use focused regression tests without a new proposal.
- Existing `docs/superpowers/specs` and `docs/superpowers/plans` files are removed. Unique product requirements must be preserved in OpenSpec before removal; implementation history remains available in Git.

## Safety

- Do not modify or discard unrelated working-tree changes.
- Check repository references before removing duplicate documents.
- This change modifies documentation and workflow conventions only.

## Verification

- No tracked files reference removed `docs/superpowers` paths.
- `git diff --check` passes.
- OpenSpec strict validation runs when the CLI is available.
