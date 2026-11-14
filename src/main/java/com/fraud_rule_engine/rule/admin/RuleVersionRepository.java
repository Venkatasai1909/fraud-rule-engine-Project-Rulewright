package com.fraud_rule_engine.rule.admin;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface RuleVersionRepository extends JpaRepository<RuleVersionEntity,UUID> {
 List<RuleVersionEntity> findByRule_IdOrderByVersionNoDesc(UUID ruleId);
 Optional<RuleVersionEntity> findTopByRule_IdOrderByVersionNoDesc(UUID ruleId);
 List<RuleVersionEntity> findByStatusOrderByPriorityAsc(RuleStatus status);
 Optional<RuleVersionEntity> findByRule_IdAndStatus(UUID ruleId,RuleStatus status);
}
