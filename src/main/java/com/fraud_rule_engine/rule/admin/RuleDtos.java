package com.fraud_rule_engine.rule.admin;
import jakarta.validation.constraints.*; import java.time.Instant; import java.util.*;
public final class RuleDtos { private RuleDtos(){}
 public record CreateRuleRequest(@NotBlank @Size(max=120) String name,@NotBlank String action,@Min(0) int priority,@NotNull Map<String,Object> condition){}
 public record CreateVersionRequest(@NotBlank String action,@Min(0) int priority,@NotNull Map<String,Object> condition){}
 public record RuleResponse(UUID ruleId,String name,String currentStatus,Integer activeVersion,Instant createdAt){}
 public record VersionResponse(UUID ruleId,String name,int version,String status,String action,int priority,Map<String,Object> condition,Instant createdAt){}
 public record ActivationResponse(UUID ruleId,int activeVersion,long rulesetVersion,String status){}
}
