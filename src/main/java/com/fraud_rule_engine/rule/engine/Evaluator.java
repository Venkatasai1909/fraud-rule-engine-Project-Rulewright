package com.fraud_rule_engine.rule.engine;
import com.fraud_rule_engine.rule.rules.EvalContext;
import java.util.ArrayList;
import java.util.List;
public final class Evaluator {
 public Decision evaluate(RuleSet set, EvalContext ctx){
  List<FiredRule> matched=new ArrayList<>(); Action strongest=null;
  for(ActiveRule r:set.rules()) if(r.compiled().matches(ctx)){ matched.add(new FiredRule(r.ruleId(),r.version(),r.action())); if(strongest==null||r.action().severity()>strongest.severity()) strongest=r.action(); }
  Verdict verdict=strongest==null?Verdict.ALLOW:(strongest==Action.BLOCK?Verdict.BLOCK:Verdict.REVIEW);
  return new Decision(verdict,set.revision(),matched);
 }
}
