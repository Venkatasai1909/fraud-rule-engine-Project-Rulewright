package com.fraud_rule_engine.rule.rules;

import java.util.Map;

/** The data a rule is evaluated against. A null return means the value is unavailable. */
public interface EvalContext {

    /** A value from the transaction request, or null if absent. */
    Object field(String name);

    /** A behavioural feature (from Redis), or null if unavailable (missing or timed out). */
    Object feature(String name);

    static EvalContext of(Map<String, ?> fields, Map<String, ?> features) {
        return new EvalContext() {
            @Override
            public Object field(String name) {
                return fields.get(name);
            }

            @Override
            public Object feature(String name) {
                return features.get(name);
            }
        };
    }
}
