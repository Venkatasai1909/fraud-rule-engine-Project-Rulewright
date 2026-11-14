package com.fraud_rule_engine.rule.admin;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;

public interface RulesetStateRepository extends JpaRepository<RulesetStateEntity, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from RulesetStateEntity s where s.id=1")
    RulesetStateEntity lockState();
}
