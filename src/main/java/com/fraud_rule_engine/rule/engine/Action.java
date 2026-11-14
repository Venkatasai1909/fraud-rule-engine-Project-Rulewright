package com.fraud_rule_engine.rule.engine;

public enum Action {
    REVIEW(1), BLOCK(2);
    private final int severity;

    Action(int s) {
        severity = s;
    }

    public int severity() {
        return severity;
    }
}
