package com.fraud_rule_engine.rule.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EvaluationLogRepository extends JpaRepository<EvaluationLogEntity, UUID> {
    Optional<EvaluationLogEntity> findByTransactionId(String transactionId);
}
