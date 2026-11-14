package com.fraud_rule_engine.rule.feature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.stereotype.Component;
import java.math.BigDecimal; import java.time.Instant; import java.util.*; import java.util.concurrent.ConcurrentHashMap;
@Component @ConditionalOnProperty(name="fraud.features.store",havingValue="memory",matchIfMissing=true)
public class InMemoryFeatureStore implements FeatureStore {
 private record Event(BigDecimal amount,Instant at) {}
 private final Map<String,Map<String,Event>> events=new ConcurrentHashMap<>();
 public Map<String,Object> recordAndGet(String customer,String txn,BigDecimal amount,Instant at) {
  var map=events.computeIfAbsent(customer,k->new ConcurrentHashMap<>()); map.putIfAbsent(txn,new Event(amount,at));
  synchronized(map) { map.values().removeIf(e->e.at().isBefore(at.minusSeconds(3600)));
   long count=map.values().stream().filter(e->!e.at().isBefore(at.minusSeconds(600))).count();
   BigDecimal sum=map.values().stream().map(Event::amount).reduce(BigDecimal.ZERO,BigDecimal::add);
   return Map.of("customer.txnCount10m",count,"customer.amountSum1h",sum);
  }
 }
}
