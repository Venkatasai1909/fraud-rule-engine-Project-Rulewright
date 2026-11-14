package com.fraud_rule_engine.rule.feature;
import java.math.BigDecimal; import java.time.Instant; import java.util.Map;
public interface FeatureStore { Map<String,Object> recordAndGet(String customerId,String transactionId,BigDecimal amount,Instant timestamp); }
