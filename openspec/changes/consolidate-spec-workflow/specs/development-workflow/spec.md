## ADDED Requirements

### Requirement: Single specification source of truth
The repository SHALL use OpenSpec as the authoritative source for behavioral requirements, architecture decisions, acceptance scenarios, approvals, and implementation task status.

#### Scenario: Planned product or architecture change
- **WHEN** work introduces a capability, breaking contract, architecture change, or security-pattern change
- **THEN** its proposal, design, requirements, scenarios, and tasks are maintained in OpenSpec without a duplicate Superpowers specification or plan

### Requirement: API contract ownership
The repository SHALL use OpenAPI as the machine-readable HTTP contract and SHALL keep it consistent with approved OpenSpec behavior.

#### Scenario: HTTP contract changes
- **WHEN** an approved change modifies request paths, payloads, response fields, or authorization behavior
- **THEN** the corresponding OpenAPI contract is updated and verified

### Requirement: Superpowers execution role
The repository SHALL use Superpowers for development execution practices without treating generated Superpowers documents as an additional specification source.

#### Scenario: Approved work is implemented
- **WHEN** an approved OpenSpec task is executed
- **THEN** applicable debugging, TDD, review, and verification workflows may be used while progress remains tracked in OpenSpec

#### Scenario: Existing behavior is repaired
- **WHEN** a bug fix restores behavior already required by the current specification
- **THEN** the fix may proceed directly with a regression test and verification without creating duplicate specification documents
