package com.fraud_rule_engine.rule.rules;

/**
 * An immutable, thread-safe, ready-to-run rule. Evaluation allocates almost nothing.
 */
public final class CompiledRule {

    private final Node root;

    CompiledRule(Node root) {
        this.root = root;
    }

    public boolean matches(EvalContext ctx) {
        return root.test(ctx);
    }

    @FunctionalInterface
    interface Node {
        boolean test(EvalContext ctx);
    }
}
