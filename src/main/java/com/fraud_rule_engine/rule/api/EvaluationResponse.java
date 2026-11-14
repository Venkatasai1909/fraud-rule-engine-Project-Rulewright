package com.fraud_rule_engine.rule.api;

import com.fraud_rule_engine.rule.engine.Verdict;

import java.util.List;

public record EvaluationResponse(Verdict decision, List<MatchedRuleResponse> matchedRules, long rulesetVersion, long evaluationMicros) {public
    EvaluationResponse {
        matchedRules = List.copyOf(matchedRules);
    }
}
