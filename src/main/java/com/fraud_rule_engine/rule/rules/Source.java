package com.fraud_rule_engine.rule.rules;

/** Where a leaf reads its value from: the request itself or a Redis feature. */
public enum Source {
    FIELD, FEATURE
}
