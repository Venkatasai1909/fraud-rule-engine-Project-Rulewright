package com.fraud_rule_engine.rule.rules;

/** A parsed and validated rule body: the condition plus the missing-feature policy. */
public record RuleDefinition(Condition condition, MissingPolicy onMissingFeature) {
}
