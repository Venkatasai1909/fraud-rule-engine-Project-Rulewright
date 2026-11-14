package com.fraud_rule_engine.rule.rules;
import java.util.Map;
public record FieldCatalog(Map<String, ValueType> fields, Map<String, ValueType> features) {
 public FieldCatalog { fields=Map.copyOf(fields); features=Map.copyOf(features); }
 public static FieldCatalog standard() {
  return new FieldCatalog(Map.ofEntries(Map.entry("transactionId",ValueType.STRING),Map.entry("customerId",ValueType.STRING),Map.entry("amount",ValueType.NUMBER),Map.entry("currency",ValueType.STRING),Map.entry("channel",ValueType.STRING),Map.entry("merchantCategory",ValueType.STRING),Map.entry("country",ValueType.STRING),Map.entry("newDevice",ValueType.BOOLEAN),Map.entry("timestamp",ValueType.STRING)),
   Map.of("customer.txnCount10m",ValueType.NUMBER,"customer.amountSum1h",ValueType.NUMBER));
 }
}
