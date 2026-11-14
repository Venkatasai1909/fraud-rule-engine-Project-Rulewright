package com.fraud_rule_engine.rule.rules;

import java.util.List;

/**
 * Thrown when a rule definition is invalid. Carries every problem found, not just the first.
 */
@SuppressWarnings("serial")
public class RuleValidationException extends RuntimeException {

    private final List<String> errors;

    public RuleValidationException(List<String> errors) {
        super("Invalid rule: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
