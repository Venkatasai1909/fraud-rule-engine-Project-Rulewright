package com.fraud_rule_engine.rule.rules;

import java.util.*;

/**
 * Parses and validates a rule definition given as plain Maps and Lists (what any JSON library
 * produces). Strict: unknown keys, fields, features and operators are rejected, and every
 * problem found is reported together.
 */
public final class RuleParser {

    public static final int MAX_DEPTH = 8;
    public static final int MAX_NODES = 100;
    public static final int MAX_LIST_VALUES = 50;

    private static final Set<String> LEAF_KEYS = Set.of("field", "feature", "op", "value");

    private final FieldCatalog catalog;

    public RuleParser(FieldCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog);
    }

    public static RuleParser standard() {
        return new RuleParser(FieldCatalog.standard());
    }

    public RuleDefinition parse(Map<String, ?> json) {
        if (json == null) {
            throw new RuleValidationException(List.of("$: rule definition is missing"));
        }
        State st = new State();
        Map<String, Object> root = new LinkedHashMap<>(json);
        MissingPolicy policy = MissingPolicy.NO_MATCH;
        if (root.containsKey("onMissingFeature")) {
            policy = parsePolicy(root.remove("onMissingFeature"), st);
        }
        Condition condition = parseNode(root, "$", 1, st);
        if (!st.errors.isEmpty()) {
            throw new RuleValidationException(st.errors);
        }
        return new RuleDefinition(condition, policy);
    }

    private MissingPolicy parsePolicy(Object raw, State st) {
        if (raw instanceof String s) {
            try {
                return MissingPolicy.valueOf(s);
            } catch (IllegalArgumentException e) {
                // fall through to the error below
            }
        }
        st.error("$.onMissingFeature", "must be NO_MATCH or MATCH");
        return MissingPolicy.NO_MATCH;
    }

    private Condition parseNode(Object raw, String path, int depth, State st) {
        if (!(raw instanceof Map<?, ?> node)) {
            st.error(path, "must be an object");
            return null;
        }
        if (depth > MAX_DEPTH) {
            st.error(path, "nesting is deeper than " + MAX_DEPTH + " levels");
            return null;
        }
        if (++st.nodes > MAX_NODES) {
            if (!st.nodeLimitReported) {
                st.error(path, "more than " + MAX_NODES + " conditions in one rule");
                st.nodeLimitReported = true;
            }
            return null;
        }
        boolean isAll = node.containsKey("all");
        boolean isAny = node.containsKey("any");
        boolean isNot = node.containsKey("not");
        boolean isLeaf = node.containsKey("field") || node.containsKey("feature");
        int kinds = (isAll ? 1 : 0) + (isAny ? 1 : 0) + (isNot ? 1 : 0) + (isLeaf ? 1 : 0);
        if (kinds != 1) {
            st.error(path, "must contain exactly one of: all, any, not, or a field/feature comparison");
            return null;
        }
        if (isAll) {
            return parseGroup(node, "all", path, depth, st);
        }
        if (isAny) {
            return parseGroup(node, "any", path, depth, st);
        }
        if (isNot) {
            return parseNot(node, path, depth, st);
        }
        return parseLeaf(node, path, st);
    }

    private Condition parseGroup(Map<?, ?> node, String key, String path, int depth, State st) {
        boolean ok = checkKeys(node, Set.of(key), path, st);
        if (!(node.get(key) instanceof List<?> items) || items.isEmpty()) {
            st.error(path + "." + key, "must be a non-empty list of conditions");
            return null;
        }
        List<Condition> children = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Condition child = parseNode(items.get(i), path + "." + key + "[" + i + "]", depth + 1, st);
            if (child == null) {
                ok = false;
            } else {
                children.add(child);
            }
        }
        if (!ok) {
            return null;
        }
        return key.equals("all") ? new Condition.All(children) : new Condition.Any(children);
    }

    private Condition parseNot(Map<?, ?> node, String path, int depth, State st) {
        boolean ok = checkKeys(node, Set.of("not"), path, st);
        Condition child = parseNode(node.get("not"), path + ".not", depth + 1, st);
        return ok && child != null ? new Condition.Not(child) : null;
    }

    private Condition parseLeaf(Map<?, ?> node, String path, State st) {
        int before = st.errors.size();
        checkKeys(node, LEAF_KEYS, path, st);
        boolean hasField = node.containsKey("field");
        if (hasField && node.containsKey("feature")) {
            st.error(path, "use either 'field' or 'feature', not both");
            return null;
        }
        Source source = hasField ? Source.FIELD : Source.FEATURE;
        String key = hasField ? "field" : "feature";
        if (!(node.get(key) instanceof String name)) {
            st.error(path + "." + key, "must be a string");
            return null;
        }
        ValueType type = (hasField ? catalog.fields() : catalog.features()).get(name);
        if (type == null) {
            st.error(path + "." + key, "unknown " + key + " '" + name + "'");
            return null;
        }
        Op op = node.get("op") instanceof String s ? Op.fromJson(s) : null;
        if (op == null) {
            st.error(path + ".op", "must be one of eq, ne, gt, gte, lt, lte, in");
            return null;
        }
        if (!type.allows(op)) {
            st.error(path + ".op", "operator '" + op.json() + "' is not allowed for " + type
                    + " " + key + " '" + name + "'");
            return null;
        }
        if (!node.containsKey("value")) {
            st.error(path + ".value", "is required");
            return null;
        }
        List<Object> values = parseValues(op, type, node.get("value"), path + ".value", st);
        if (values == null || st.errors.size() > before) {
            return null;
        }
        return new Condition.Leaf(source, name, type, op, values);
    }

    private List<Object> parseValues(Op op, ValueType type, Object raw, String path, State st) {
        if (op == Op.IN) {
            if (!(raw instanceof List<?> items) || items.isEmpty() || items.size() > MAX_LIST_VALUES) {
                st.error(path, "must be a list of 1 to " + MAX_LIST_VALUES + " values");
                return null;
            }
            List<Object> out = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                Object v = coerce(items.get(i), type, path + "[" + i + "]", st);
                if (v != null) {
                    out.add(v);
                }
            }
            return out.size() == items.size() ? out : null;
        }
        Object single = coerce(raw, type, path, st);
        return single == null ? null : List.of(single);
    }

    private Object coerce(Object raw, ValueType type, String path, State st) {
        Object value = switch (type) {
            case NUMBER -> Numbers.toBigDecimalOrNull(raw);
            case STRING -> raw instanceof String s ? s : null;
            case BOOLEAN -> raw instanceof Boolean b ? b : null;
        };
        if (value == null) {
            st.error(path, "must be a " + type.name().toLowerCase());
        }
        return value;
    }

    private boolean checkKeys(Map<?, ?> node, Set<String> allowed, String path, State st) {
        boolean ok = true;
        for (Object key : node.keySet()) {
            if (!allowed.contains(key)) {
                st.error(path, "unknown key '" + key + "'");
                ok = false;
            }
        }
        return ok;
    }

    private static final class State {
        final List<String> errors = new ArrayList<>();
        int nodes;
        boolean nodeLimitReported;

        void error(String path, String message) {
            errors.add(path + ": " + message);
        }
    }
}
