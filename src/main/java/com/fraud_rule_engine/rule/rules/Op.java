package com.fraud_rule_engine.rule.rules;
public enum Op {
 EQ("eq"), NE("ne"), GT("gt"), GTE("gte"), LT("lt"), LTE("lte"), IN("in");
 private final String json; Op(String json){this.json=json;} public String json(){return json;}
 public static Op fromJson(String text){for(Op op:values()) if(op.json.equals(text)) return op; return null;}
}
