package com.fraud_rule_engine.rule.engine;

import java.util.List;

public record Decision(Verdict verdict, long rulesetVersion, List<FiredRule> matchedRules) { public Decision {
    matchedRules = List.copyOf(matchedRules);
}
}
