package com.fraud_rule_engine.rule.rules;

import java.util.List;

/**
 * Validated condition tree. Built only by {@link RuleParser}.
 */
public sealed

interface Condition permits Condition.All, Condition.Any, Condition.Not, Condition.Leaf {

        Condition {
        public All {
            children = List.copyOf(children);
        }
    } implements

    Condition {
        public Any {
            children = List.copyOf(children);
        }
    }

        Condition {
    } implements

    Condition {
        public Leaf {
            values = List.copyOf(values);
        }
    }

    record All(List<Condition> children) implements

record Any(List<Condition> children)

    record Not(Condition child) implements

/**
     * One comparison. {@code values} holds BigDecimal, String or Boolean items depending on
     * {@code type}: one item for scalar operators, one or more for IN.
     */
    record Leaf(Source source, String name, ValueType type, Op op, List<Object> values)
}
