package com.fraud_rule_engine.rule.engine;

import java.util.UUID;

public record FiredRule(UUID ruleId, int version, Action action) {
}
