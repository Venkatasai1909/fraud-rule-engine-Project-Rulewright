package com.fraud_rule_engine.rule.feature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.data.redis.core.StringRedisTemplate; import org.springframework.stereotype.Component;
@Component @ConditionalOnProperty(name="fraud.features.store",havingValue="redis")
public class RedisRuleChangeNotifier implements RuleChangeNotifier {
 private final StringRedisTemplate redis; public RedisRuleChangeNotifier(StringRedisTemplate redis){this.redis=redis;}
 public void publish(long version){redis.convertAndSend("fraud:ruleset:changed",Long.toString(version));}
}
