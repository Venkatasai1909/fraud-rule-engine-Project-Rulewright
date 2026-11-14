package com.fraud_rule_engine.rule.rules;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Turns a validated {@link RuleDefinition} into a {@link CompiledRule}.
 *
 * <p>Semantics: a null request field never matches any comparison. A null (unavailable) feature
 * returns the rule's {@code onMissingFeature} policy for that comparison. A value of the wrong
 * runtime type, NaN or infinity never matches. A NOT inverts whatever its child returned.
 */
public final class RuleCompiler {

    private RuleCompiler() {
    }

    public static CompiledRule compile(RuleDefinition definition) {
        boolean matchWhenMissing = definition.onMissingFeature() == MissingPolicy.MATCH;
        return new CompiledRule(node(definition.condition(), matchWhenMissing));
    }

    private static CompiledRule.Node node(Condition condition, boolean matchWhenMissing) {
        return switch (condition) {
            case Condition.All all -> {
                CompiledRule.Node[] kids = all.children().stream()
                        .map(k -> node(k, matchWhenMissing)).toArray(CompiledRule.Node[]::new);
                yield ctx ->{
                    for (CompiledRule.Node kid : kids) {
                        if (!kid.test(ctx)) {
                            return false;
                        }
                    }
                    return true;
                }
                ;
            }
            case Condition.Any any -> {
                CompiledRule.Node[] kids = any.children().stream()
                        .map(k -> node(k, matchWhenMissing)).toArray(CompiledRule.Node[]::new);
                yield ctx ->{
                    for (CompiledRule.Node kid : kids) {
                        if (kid.test(ctx)) {
                            return true;
                        }
                    }
                    return false;
                }
                ;
            }
            case Condition.Not not -> {
                CompiledRule.Node kid = node(not.child(), matchWhenMissing);
                yield ctx ->!kid.test(ctx);
            }
            case Condition.Leaf leaf -> leafNode(leaf, matchWhenMissing);
        };
    }

    private static CompiledRule.Node leafNode(Condition.Leaf leaf, boolean matchWhenMissing) {
        String name = leaf.name();
        boolean isFeature = leaf.source() == Source.FEATURE;
        boolean whenMissing = isFeature && matchWhenMissing;
        Predicate<Object> test = switch (leaf.type()) {
            case NUMBER -> numberTest(leaf);
            case STRING -> stringTest(leaf);
            case BOOLEAN -> booleanTest(leaf);
        };
        return ctx -> {
            Object actual = isFeature ? ctx.feature(name) : ctx.field(name);
            return actual == null ? whenMissing : test.test(actual);
        };
    }

    private static Predicate<Object> numberTest(Condition.Leaf leaf) {
        List<BigDecimal> refs = leaf.values().stream().map(v -> (BigDecimal) v).toList();
        BigDecimal a = refs.get(0);
        BigDecimal b = refs.size() > 1 ? refs.get(1) : a;
        return switch (leaf.op()) {
            case EQ -> num(x -> x.compareTo(a) == 0);
            case NE -> num(x -> x.compareTo(a) != 0);
            case GT -> num(x -> x.compareTo(a) > 0);
            case GTE -> num(x -> x.compareTo(a) >= 0);
            case LT -> num(x -> x.compareTo(a) < 0);
            case LTE -> num(x -> x.compareTo(a) <= 0);
            case IN -> num(x -> refs.stream().anyMatch(r -> x.compareTo(r) == 0));
        };
    }

    private static Predicate<Object> num(Predicate<BigDecimal> test) {
        return value -> {
            BigDecimal x = Numbers.toBigDecimalOrNull(value);
            return x != null && test.test(x);
        };
    }

    private static Predicate<Object> stringTest(Condition.Leaf leaf) {
        List<String> refs = leaf.values().stream().map(v -> (String) v).toList();
        String a = refs.get(0);
        return switch (leaf.op()) {
            case EQ -> v -> v instanceof String s && s.equals(a);
            case NE -> v -> v instanceof String s && !s.equals(a);
            case IN -> {
                Set<String> set = Set.copyOf(refs);
                yield v ->v instanceof String s && set.contains(s);
            }
            default -> throw new IllegalStateException("Operator not valid for strings: " + leaf.op());
        };
    }

    private static Predicate<Object> booleanTest(Condition.Leaf leaf) {
        Boolean a = (Boolean) leaf.values().get(0);
        Set<Boolean> refs = Set.copyOf(leaf.values().stream().map(Boolean.class::cast).toList());
        return switch (leaf.op()) {
            case EQ -> v -> v instanceof Boolean x && x.equals(a);
            case NE -> v -> v instanceof Boolean x && !x.equals(a);
            case IN -> v -> v instanceof Boolean x && refs.contains(x);
            default -> throw new IllegalStateException("Operator not valid for booleans: " + leaf.op());
        };
    }
}
