# Tracing Debug Runbook (Jaeger + OTEL Collector)

Use this runbook to debug end-to-end request flow across:
- HTTP API
- Kafka produce/consume
- Persistence (repository spans)

## 1. Access
- Jaeger UI: `http://localhost:16686`
- OTEL Collector OTLP HTTP ingest: `http://localhost:4318`
- OTEL Collector OTLP gRPC ingest: `localhost:4317`

## 2. Services To Query In Jaeger
- `order-microservice`
- `payment-microservice`
- `product-microservice`

## 3. Recommended Jaeger Saved Searches
Create these as saved searches in Jaeger UI.

### Search A: Checkout Entry
- Service: `order-microservice`
- Lookback: `Last 15 minutes`
- Purpose: find root API spans for order creation and follow the trace into Kafka.

### Search B: Payment Processing
- Service: `payment-microservice`
- Lookback: `Last 15 minutes`
- Purpose: inspect order-created consumption and payment processing/publish spans.

### Search C: Inventory Reduction
- Service: `product-microservice`
- Lookback: `Last 15 minutes`
- Purpose: verify `product-updated` consume path and persistence spans.

### Search D: Error Traces
- Service: `product-microservice`
- Tags: `error=true`
- Lookback: `Last 30 minutes`
- Purpose: identify failed inventory processing traces that usually end in DLT.

### Search E: Order Payment Listener
- Service: `order-microservice`
- Lookback: `Last 30 minutes`
- Purpose: inspect payment-result consumption and order status update path.

## 4. Expected Trace Shape (Happy Path)
1. `order-microservice` HTTP span for `POST /api/order`
2. Kafka producer span from order service (`order-created` topic)
3. Kafka consumer span in `payment-microservice` (`order-created`)
4. Payment service processing spans and repository spans
5. Kafka producer span from payment (`order-payment` topic)
6. Kafka consumer span in `order-microservice` (`order-payment`)
7. Kafka producer span from order (`product-updated` topic)
8. Kafka consumer span in `product-microservice` (`product-updated`)
9. Product repository spans showing inventory update

## 5. Failure Playbooks

### A) Payment Duplicate / Non-PENDING Failures
Symptoms:
- `uk_payment_order_id` duplicate key
- `Payment must be in PENDING status to be processed`

Steps:
1. Query `payment-microservice` traces around failure time.
2. Open trace with `error=true` spans.
3. Confirm if payment was created and processed more than once in the same order flow.
4. Cross-check corresponding `order-microservice` trace to ensure idempotent event handling path executed once.

### B) Inventory Event Sent To DLT
Symptoms:
- Logs include `Product event sent to DLT` in `product-microservice`.

Steps:
1. Query `product-microservice` with `error=true`.
2. Open failing trace and inspect failing consumer/persistence span.
3. Verify topic flow includes `product-updated` consume before failure.
4. Correlate exception with repository/database span details.

## 6. Fast Correlation From Logs To Jaeger
Logs include:
- `trace=<traceId>`
- `span=<spanId>`

Workflow:
1. Copy `traceId` from service logs.
2. In Jaeger, open trace search and paste trace ID.
3. Inspect full cross-service trace tree.

## 7. Quick Checks If No Traces Appear
1. Check collector and Jaeger are running:
   - `docker-compose ps | grep -E 'otel-collector|jaeger'`
2. Check collector logs:
   - `docker-compose logs -f otel-collector`
3. Check a microservice exports to collector endpoint:
   - env `OTLP_TRACING_ENDPOINT=http://otel-collector:4318/v1/traces`
4. Check service logs contain `trace=` and `span=` fields.

