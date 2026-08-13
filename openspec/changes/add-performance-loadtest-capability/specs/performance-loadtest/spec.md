## ADDED Requirements

### Requirement: Scenario-driven load execution
The system SHALL support running performance tests from versioned scenario definitions in addition to direct CLI flags.

#### Scenario: Run a named scenario from file
- **WHEN** a user runs the performance-loadtest module with a scenario file and a scenario name
- **THEN** the runner loads that scenario's HTTP request and load settings
- **AND** executes the load test using those settings without requiring duplicate CLI arguments

#### Scenario: Fall back to direct CLI mode
- **WHEN** a user runs the performance-loadtest module with direct CLI options and no scenario file
- **THEN** the runner executes a single ad-hoc test using the provided options

### Requirement: Rich HTTP request configuration
The system SHALL allow a scenario to define request metadata needed for realistic endpoint testing.

#### Scenario: Include headers in a scenario
- **WHEN** a scenario defines one or more HTTP headers
- **THEN** the runner includes those headers in each request it sends

#### Scenario: Load request body from file
- **WHEN** a scenario references a body file for a write request
- **THEN** the runner loads the file contents and uses them as the request body

### Requirement: Multi-step scenario execution
The system SHALL allow a scenario to execute an ordered sequence of HTTP requests while carrying extracted values across steps.

#### Scenario: Extract a value from one step and reuse it later
- **WHEN** an earlier scenario step extracts a response field such as an order ID or payment ID
- **THEN** a later step can reference that extracted value in its URL, headers, or body

#### Scenario: Run an end-to-end checkout workflow
- **WHEN** a scenario defines a cart request, order request, payment lookup/process requests, and a verification request
- **THEN** the runner executes the steps in order as a single virtual-user workflow
- **AND** reports the combined latency and failure characteristics of that workflow

### Requirement: Step-level response assertions
The system SHALL allow a scenario step to assert conditions against the JSON response body using extracted workflow values.

#### Scenario: Verify inventory volume changed after payment
- **WHEN** an earlier step extracts a product volume and a later verification step asserts that the current volume is lower
- **THEN** the runner retries the verification step as configured until the assertion passes or attempts are exhausted

#### Scenario: Fail workflow when assertion never passes
- **WHEN** a verification step completes with the expected HTTP status but its configured assertions are not satisfied
- **THEN** the runner marks the workflow as failed
- **AND** records the assertion failure in the workflow error output

### Requirement: Threshold-based performance gating
The system SHALL evaluate optional performance thresholds after a run and fail the process when a threshold is breached.

#### Scenario: Thresholds pass
- **WHEN** a run completes and all configured thresholds are satisfied
- **THEN** the runner exits successfully
- **AND** the report marks the scenario as passed

#### Scenario: Thresholds fail
- **WHEN** a run completes and any configured latency, throughput, or error-rate threshold is violated
- **THEN** the runner exits with a non-zero status
- **AND** the report identifies which thresholds failed

### Requirement: Report artifacts for baseline tracking
The system SHALL produce machine-readable artifacts that are suitable for baseline comparison and CI ingestion.

#### Scenario: Report includes scenario context
- **WHEN** a run finishes
- **THEN** the generated report includes the scenario name or CLI label, request settings, summary metrics, and threshold results

#### Scenario: Report directory is configurable
- **WHEN** a user specifies a report directory
- **THEN** the runner writes its generated artifacts to that directory
