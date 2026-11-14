# Versioned Fraud Rules and Evaluation API (Task 1)

Spring Boot 4 / Java 21 service. Rules are JSON condition trees, validated at write time, compiled into immutable in-memory snapshots, and evaluated without querying rule tables on the request path. PostgreSQL stores rule versions, the global ruleset version, and idempotent evaluation logs.

## Run

Requires Java 21 and PostgreSQL. Create a database named `fraud_rules`, then configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` if needed. Start with `./mvnw spring-boot:run`. Flyway creates the schema and seeds eight active example rules.

## Rule condition format

```json
{ "all": [
  { "field": "channel", "op": "eq", "value": "ECOM" },
  { "any": [
    { "field": "amount", "op": "gte", "value": 50000 },
    { "not": { "field": "newDevice", "op": "eq", "value": false } }
  ]}
]}
```

Groups: `all`, `any`, `not`. Leaf operators: `eq`, `ne`, `gt`, `gte`, `lt`, `lte`, `in`. Valid fields: `transactionId`, `customerId`, `amount`, `currency`, `channel`, `merchantCategory`, `country`, `newDevice`, `timestamp`.

## Curl examples

Create rule (response contains `ruleId` and `version: 1`):

```bash
curl -X POST http://localhost:8080/api/v1/rules -H 'Content-Type: application/json' -d '{"name":"New device high value","action":"BLOCK","priority":10,"condition":{"all":[{"field":"amount","op":"gte","value":100000},{"field":"newDevice","op":"eq","value":true}]}}'
```

Activate version 1 (replace `<RULE_ID>`):

```bash
curl -X POST http://localhost:8080/api/v1/rules/<RULE_ID>/versions/1/activate
```

Evaluate a transaction:

```bash
curl -X POST http://localhost:8080/api/v1/evaluate -H 'Content-Type: application/json' -d '{"transactionId":"txn-1001","customerId":"cust-9","amount":125000,"currency":"INR","channel":"ECOM","merchantCategory":"5732","country":"IN","newDevice":true,"timestamp":"2026-11-14T10:15:30Z"}'
```

Create version 2 and activate it:

```bash
curl -X POST http://localhost:8080/api/v1/rules/<RULE_ID>/versions -H 'Content-Type: application/json' -d '{"action":"BLOCK","priority":5,"condition":{"field":"amount","op":"gte","value":150000}}'
curl -X POST http://localhost:8080/api/v1/rules/<RULE_ID>/versions/2/activate
```

Retrieve a stored evaluation: `GET /api/v1/evaluations/txn-1001`. Repeating an evaluation with the same `transactionId` returns the original persisted response, including the original matched versions and ruleset version.

## API summary

- `POST /api/v1/rules` — create rule and DRAFT version 1.
- `POST /api/v1/rules/{ruleId}/versions` — create next DRAFT version.
- `POST /api/v1/rules/{ruleId}/versions/{version}/activate` — activate version and increment global `rulesetVersion`.
- `POST /api/v1/rules/{ruleId}/deactivate` — deactivate active version and increment global version.
- `GET /api/v1/rules` — current state.
- `GET /api/v1/rules/{ruleId}/versions` — immutable version history.
- `POST /api/v1/evaluate` — evaluate using one in-memory snapshot and persist result.
- `GET /api/v1/evaluations/{transactionId}` — retrieve stored result.

Decision precedence is `BLOCK > REVIEW > ALLOW`. A matched rule is returned with `ruleId`, exact `version`, and `action`.

## Tests and generator

`./mvnw test` runs pure unit tests plus H2-backed MockMvc API integration tests; the test profile does not require Docker. A synthetic transaction generator is in `tools/generate_transactions.py`.


## Task 2
See [TASK2_COMPLETION.md](fraud-rule-engine-task2/TASK2_COMPLETION.md) for Redis-backed behavioural features, idempotency, hot reload, failure policy, and the 2,000-request load test. The default `FEATURE_STORE=memory` keeps tests Redis-free; use `FEATURE_STORE=redis` for multi-instance Redis deployments.
