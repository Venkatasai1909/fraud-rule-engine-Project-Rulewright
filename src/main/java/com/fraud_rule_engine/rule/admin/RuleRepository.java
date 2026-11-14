package com.fraud_rule_engine.rule.admin;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface RuleRepository extends JpaRepository<RuleEntity,UUID> {}
