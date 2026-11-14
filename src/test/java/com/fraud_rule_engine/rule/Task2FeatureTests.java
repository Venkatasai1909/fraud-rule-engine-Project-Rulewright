package com.fraud_rule_engine.rule;
import com.fraud_rule_engine.rule.feature.InMemoryFeatureStore;
import com.fraud_rule_engine.rule.rules.RuleParser;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal; import java.time.Instant; import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class Task2FeatureTests {
 @Test void featureFieldsAreAcceptedByRuleCatalog(){
  var parsed=RuleParser.standard().parse(Map.of("feature","customer.txnCount10m","op","gte","value",5));
  assertNotNull(parsed);
  assertNotNull(RuleParser.standard().parse(Map.of("feature","customer.amountSum1h","op","gte","value",1000)));
 }
 @Test void inMemoryFeatureUpdateIsIdempotent(){
  var store=new InMemoryFeatureStore(); var at=Instant.parse("2026-11-14T10:00:00Z");
  var first=store.recordAndGet("c1","t1",new BigDecimal("100.50"),at);
  var replay=store.recordAndGet("c1","t1",new BigDecimal("100.50"),at);
  assertEquals(1L,first.get("customer.txnCount10m"));
  assertEquals(1L,replay.get("customer.txnCount10m"));
  assertEquals(new BigDecimal("100.50"),replay.get("customer.amountSum1h"));
 }
}
