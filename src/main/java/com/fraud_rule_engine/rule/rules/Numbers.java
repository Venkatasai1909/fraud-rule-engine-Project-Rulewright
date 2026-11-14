package com.fraud_rule_engine.rule.rules;

import java.math.BigDecimal;
import java.math.BigInteger;

/** Number conversion shared by the parser and the compiled rules. Money is compared exactly. */
final class Numbers {

    private Numbers() {
    }

    /** Converts any Number to BigDecimal; returns null for non-numbers, NaN and infinities. */
    static BigDecimal toBigDecimalOrNull(Object value) {
        if (!(value instanceof Number n)) {
            return null;
        }
        try {
            if (n instanceof BigDecimal b) {
                return b;
            }
            if (n instanceof Integer || n instanceof Long || n instanceof Short || n instanceof Byte) {
                return BigDecimal.valueOf(n.longValue());
            }
            if (n instanceof BigInteger bi) {
                return new BigDecimal(bi);
            }
            return new BigDecimal(n.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
