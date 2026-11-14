package com.fraud_rule_engine.rule.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleVersionRepository extends JpaRepository<RuleVersionEntity, UUID> {
    List<RuleVersionEntity> findByRule_IdOrderByVersionNoDesc(UUID ruleId);

    Optional<RuleVersionEntity> findTopByRule_IdOrderByVersionNoDesc(UUID ruleId);

    List<RuleVersionEntity> findByStatusOrderByPriorityAsc(RuleStatus status);

    Optional<RuleVersionEntity> findByRule_IdAndStatus(UUID ruleId, RuleStatus status);
}
