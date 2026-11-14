package com.fraud_rule_engine.rule.feature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.stereotype.Component;
@Component @ConditionalOnProperty(name="fraud.features.store",havingValue="memory",matchIfMissing=true)
public class NoopRuleChangeNotifier implements RuleChangeNotifier { public void publish(long version){} }
