package com.fraud_rule_engine.rule.engine;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds the live RuleSet for this instance. Readers call {@link #current()} once per request
 * and use that one snapshot throughout. Writers swap in a fully built RuleSet atomically, and
 * only forward: an older or equal revision is ignored, so a late or duplicate change signal can
 * never roll an instance back.
 */
public final class SnapshotHolder {

    private final AtomicReference<RuleSet> ref = new AtomicReference<>(RuleSet.empty());

    public RuleSet current() {
        return ref.get();
    }

    /** False until the first real revision is loaded. Use it for the readiness probe. */
    public boolean isLoaded() {
        return ref.get().revision() > 0;
    }

    /** Installs {@code next} if its revision is newer than the current one. Returns true if installed. */
    public boolean swapIfNewer(RuleSet next) {
        Objects.requireNonNull(next);
        while (true) {
            RuleSet cur = ref.get();
            if (next.revision() <= cur.revision()) {
                return false;
            }
            if (ref.compareAndSet(cur, next)) {
                return true;
            }
        }
    }
}
