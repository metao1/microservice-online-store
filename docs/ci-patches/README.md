# CI workflow patches

GitHub blocks the v0 / github-actions bot from pushing changes under
`.github/workflows/**` unless its token is granted the `workflows` permission.
Any workflow change proposed by v0 is parked here as a `.patch` file so a
human maintainer can apply it with a single command.

## Applying a patch

From the repo root:

```bash
git apply docs/ci-patches/<name>.patch
git add .github/workflows/
git commit -m "ci: <summary>"
git push
```

If `git apply` rejects a hunk (because the workflow file has drifted),
use the three-way merge flavor:

```bash
git apply --3way docs/ci-patches/<name>.patch
```

## Current patches

### `performance-loadtest-review.workflows.patch`

Two changes needed by PR "performance-loadtest review":

1. `gradle.yml` — add `performance-loadtest/**` to the `paths:` filter so
   the module's unit tests run on PRs that touch it. Without this, the 16
   tests the load-test framework ships with never execute in CI.
2. `performance-slo.yml` — new nightly + manual workflow that boots the
   Docker Compose stack, waits on actuator health, runs three representative
   scenarios (`payment-status-page`, `inventory-category-page-ramp`,
   `bookstore-e2e-order-payment-inventory`), and uploads reports + service
   logs as artifacts. The runner already exits non-zero on threshold /
   baseline breach, so this wires the SLO gate without adding new assertion
   code. Matrix jobs are serialized via a `concurrency` group because
   performance numbers are meaningless under contended CPU.

After applying, the workflow will also need repo secrets / permissions
review if you want it to run on forks; as shipped it runs on `schedule` and
`workflow_dispatch` only, so no PR-from-fork exposure.
