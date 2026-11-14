package com.fraud_rule_engine.rule.propagation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@ConditionalOnProperty(name="fraud.features.store",havingValue="redis")
class RedisRuleSetSubscriber {
 @Bean RedisMessageListenerContainer ruleMessageContainer(RedisConnectionFactory factory, RuleSetRefreshService refresh) {
  var container=new RedisMessageListenerContainer();
  container.setConnectionFactory(factory);
  container.addMessageListener((MessageListener)(Message message, byte[] pattern)->refresh.refresh(),new ChannelTopic("fraud:ruleset:changed"));
  return container;
 }
}
