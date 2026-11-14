package com.fraud_rule_engine.rule.rules;

/** Data type of a request field or a behavioural feature. */
public enum ValueType {
    NUMBER, STRING, BOOLEAN;

    public boolean allows(Op op) {
        return switch (this) {
            case NUMBER -> true;
            case STRING -> op == Op.EQ || op == Op.NE || op == Op.IN;
            case BOOLEAN -> op == Op.EQ || op == Op.NE || op == Op.IN;
        };
    }
}
