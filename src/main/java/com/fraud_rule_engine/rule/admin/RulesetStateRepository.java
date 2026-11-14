package com.fraud_rule_engine.rule.admin;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType;
public interface RulesetStateRepository extends JpaRepository<RulesetStateEntity,Integer> { @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from RulesetStateEntity s where s.id=1") RulesetStateEntity lockState(); }
