package com.fraud_rule_engine.rule.engine;

import java.util.Comparator;
import java.util.List;

/**
 * An immutable snapshot of every live rule at one published revision. A request evaluates
 * against exactly one RuleSet, so its decision always names a single consistent revision.
 */
public record RuleSet(long revision, List<ActiveRule> rules) {

    public RuleSet {
        if (revision < 0) {
            throw new IllegalArgumentException("revision must not be negative");
        }
        rules = rules.stream()
                .sorted(Comparator.comparingInt(ActiveRule::priority)
                        .thenComparing(ActiveRule::name)
                        .thenComparing(ActiveRule::ruleId))
                .toList();
    }

    /** Revision 0 means "nothing loaded yet". */
    public static RuleSet empty() {
        return new RuleSet(0, List.of());
    }
}
