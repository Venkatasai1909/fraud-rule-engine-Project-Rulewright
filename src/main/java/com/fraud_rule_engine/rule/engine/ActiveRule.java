package com.fraud_rule_engine.rule.engine;

import com.fraud_rule_engine.rule.rules.CompiledRule;

import java.util.UUID;

public record ActiveRule(UUID ruleId, String name, int version, Action action, int priority, Mode mode, CompiledRule compiled) {
}
