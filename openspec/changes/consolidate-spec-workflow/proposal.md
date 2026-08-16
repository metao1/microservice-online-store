# Change: Consolidate specification workflow

## Why

The repository currently stores overlapping design and implementation documents in both OpenSpec and `docs/superpowers`, creating competing sources of truth and unnecessary process overhead.

## What Changes

- Establish OpenSpec as the sole repository source for requirements, design decisions, scenarios, approvals, and task status.
- Retain OpenAPI as the machine-readable HTTP contract.
- Use Superpowers only as an execution discipline for debugging, TDD, review, and verification.
- Remove existing duplicate Superpowers specifications and plans after confirming no repository references depend on them.

## Impact

- Affected process: repository development and specification workflow.
- Affected files: `AGENTS.md`, `openspec/project.md`, and `docs/superpowers/**`.
- Runtime behavior and API contracts are unchanged.
