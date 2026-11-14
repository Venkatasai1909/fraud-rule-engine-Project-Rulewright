# Project Rulewright — Fraud Rule Engine

A Spring Boot service for managing versioned fraud rules, evaluating synthetic payment transactions against an in-memory compiled ruleset, recording decisions, enriching transactions with Redis-backed behavioural features, and propagating rule changes between application instances.

> **Submission accuracy note:** This documentation bundle supplements the Task 1 + Task 2 project. It does not include the Task 3 React UI. At the time this bundle was prepared, the inspected `pom.xml` declared Spring Boot `4.1.2`, while the assignment explicitly asks for Spring Boot 3.x. Resolve that compatibility requirement before submission. The build, integration tests, two-instance demo, and benchmark must be run in your environment; no passing result or latency number is claimed here.

## Architecture

See [docs/architecture.md](docs/architecture.md) for the component diagram and request/propagation flows.

At a high level:

- PostgreSQL stores rule definitions, immutable versions, global `rulesetVersion`, and evaluation audit records.
- Rule versions are validated and compiled into immutable in-memory snapshots.
- The evaluation endpoint reads a snapshot and applies `BLOCK > REVIEW > ALLOW` precedence.
- `FeatureStore` abstracts in-memory and Redis-backed behavioural features.
- Redis Sorted Sets hold customer transaction events for sliding-window calculations; Lua provides atomic deduplication/update.
- Redis Pub/Sub announces committed rule changes. Each instance reloads from PostgreSQL and atomically swaps its complete snapshot. Periodic PostgreSQL version polling is the fallback.

## Requirements

- Java 21 (as configured in the current POM; verify the required Java version in your final build environment)
- Maven Wrapper (`./mvnw`)
- Docker Compose plugin
- `curl` and Python 3 for helper scripts

## Local setup

1. Copy `.env.example` to `.env` and adjust credentials/ports if needed.
2. Start PostgreSQL and Redis:

   ```bash
   docker compose --env-file .env up -d
   docker compose --env-file .env ps
   ```

3. Export the application environment (the app runs on the host, not in Compose):

   ```bash
   set -a
   source .env
   set +a
   ```

4. Run the application with Redis enabled:

   ```bash
   FEATURE_STORE=redis ./mvnw spring-boot:run
   ```

   Flyway applies the database migration on startup. The migration seeds synthetic rules.

For a Redis-free local run, use `FEATURE_STORE=memory`; the database is still required by the application.

## Tests

Plain unit/API tests are intended to run without Docker or Redis:

```bash
./mvnw test
```

Run this command in a clean environment before submission and record the actual result. Redis/PostgreSQL integration checks should be kept separate from the plain test run if they require live services or Testcontainers.

## API examples

Create a rule (version 1 is created as DRAFT):

```bash
curl -X POST http://localhost:8080/api/v1/rules \
  -H 'Content-Type: application/json' \
  -d '{"name":"High value new device","action":"REVIEW","priority":10,"condition":{"all":[{"field":"amount","op":"gte","value":50000},{"field":"newDevice","op":"eq","value":true}]}}'
```

Activate version 1 (replace the UUID):

```bash
curl -X POST http://localhost:8080/api/v1/rules/<RULE_ID>/versions/1/activate
```

Evaluate a synthetic transaction:

```bash
curl -X POST http://localhost:8080/api/v1/evaluate \
  -H 'Content-Type: application/json' \
  -d '{"transactionId":"txn-demo-1001","customerId":"customer-42","amount":75000,"currency":"INR","channel":"ECOM","merchantCategory":"5732","country":"IN","newDevice":true,"timestamp":"2026-11-14T10:15:30Z"}'
```

Retrieve the persisted decision:

```bash
curl http://localhost:8080/api/v1/evaluations/txn-demo-1001
```

## Task 2: Redis features and hot reload

The intended behavioural fields are:

- `customer.txnCount10m` — count of this customer's transactions in the last ten minutes.
- `customer.amountSum1h` — sum of this customer's transaction amounts in the last hour.

The current Task 2 implementation notes describe the field leaf syntax as `feature`. Verify and, if necessary, update the parser/tests so the assignment's required ordinary leaf syntax `{ "field": "customer.txnCount10m", ... }` is accepted before submission.

Redis feature retrieval is configured with a 100ms connect/read timeout and a fail-open policy. When features are unavailable, feature-dependent conditions follow the engine's missing-feature policy; ordinary transaction fields remain evaluable. The sorted-set/dedup TTL is currently documented as two hours, so be explicit about the supported replay horizon and its implications.

### Two-instance hot reload demo

With PostgreSQL and Redis running, execute:

```bash
./scripts/hot-reload-demo.sh
```

The script starts two local application processes on ports 8081 and 8082, creates and activates a rule through instance 1, then polls instance 2's `/internal/ready` endpoint for the new ruleset version. It requires the Maven wrapper to be able to run and uses `curl` and Python 3. Run it and retain the actual output; do not report success unless it completes successfully.

## Load test and measured latency

The repository's `tools/task2_load_test.py` is intended to create at least 50 active rules and issue 2,000 evaluations at concurrency 20, printing p50 and p95 HTTP latency. Start PostgreSQL and Redis, run the application with `FEATURE_STORE=redis`, then execute:

```bash
python3 tools/task2_load_test.py
```

**Measured p50:** _Record the actual value here._  
**Measured p95:** _Record the actual value here._  
**Environment:** _Record CPU, memory, OS, Docker limits, and whether the app ran on the host or in a container._

The assignment target is p95 below 50ms. Do not replace the placeholders with estimates. Benchmark the complete HTTP path, including Redis and PostgreSQL persistence.

## Synthetic data

Generate synthetic transaction payloads with:

```bash
python3 tools/generate_transactions.py
```

Use synthetic identifiers and data only. Do not commit real customer or payment data.

## Security and configuration

- Do not commit `.env`, credentials, access tokens, or production data.
- `.env.example` contains local development defaults only.
- Use environment variables for database and Redis connection details.
- Keep generated logs, build outputs, and IDE files out of Git.

## Current scope and next steps

- Task 1 and Task 2 backend scope is represented in the project.
- Task 3 React + TypeScript analyst UI is not included in this backend supplement.
- Before submission: resolve Spring Boot 3.x compatibility; verify field syntax for behavioural features; add/execute injectable-clock window tests; run `./mvnw test` offline; execute the two-instance demo; run and record the 2,000-request benchmark; review transaction idempotency/concurrency behaviour; and update this README with measured evidence.
- See [AI_USAGE.md](AI_USAGE.md) for the AI-use disclosure draft and required candidate confirmation.
