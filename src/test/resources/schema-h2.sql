CREATE TABLE IF NOT EXISTS fraud_rule (
  id UUID PRIMARY KEY,
  name VARCHAR(120) NOT NULL UNIQUE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE IF NOT EXISTS rule_version (
  id UUID PRIMARY KEY,
  rule_id UUID NOT NULL REFERENCES fraud_rule(id),
  version_no INTEGER NOT NULL,
  status VARCHAR(16) NOT NULL,
  action VARCHAR(16) NOT NULL,
  priority INTEGER NOT NULL,
  condition_json JSON NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT uq_rule_version UNIQUE(rule_id, version_no)
);
CREATE TABLE IF NOT EXISTS ruleset_state (id INTEGER PRIMARY KEY, version BIGINT NOT NULL);
CREATE TABLE IF NOT EXISTS evaluation_log (
  id UUID PRIMARY KEY,
  transaction_id VARCHAR(160) NOT NULL UNIQUE,
  decision VARCHAR(16) NOT NULL,
  ruleset_version BIGINT NOT NULL,
  matched_rules JSON NOT NULL,
  request_payload JSON NOT NULL,
  evaluation_micros BIGINT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
