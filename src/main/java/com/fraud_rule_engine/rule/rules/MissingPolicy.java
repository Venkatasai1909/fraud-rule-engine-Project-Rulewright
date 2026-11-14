package com.fraud_rule_engine.rule.rules;

/** What a feature comparison returns when the feature is unavailable (for example Redis timed out). */
public enum MissingPolicy {
    NO_MATCH, MATCH
}
