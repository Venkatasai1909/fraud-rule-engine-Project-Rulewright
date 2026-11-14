package com.fraud_rule_engine.rule.api;
import com.fraud_rule_engine.rule.engine.Action; import java.util.UUID;
public record MatchedRuleResponse(UUID ruleId,int version,Action action){}
