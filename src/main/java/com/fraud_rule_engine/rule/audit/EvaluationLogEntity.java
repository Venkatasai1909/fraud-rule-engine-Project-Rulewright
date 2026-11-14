package com.fraud_rule_engine.rule.audit;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "evaluation_log")
public class EvaluationLogEntity {
    @Id
    private UUID id;
    @Column(name = "transaction_id", nullable = false, unique = true, length = 160)
    private String transactionId;
    @Column(nullable = false, length = 16)
    private String decision;
    @Column(name = "ruleset_version", nullable = false)
    private long rulesetVersion;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matched_rules", nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> matchedRules;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> requestPayload;
    @Column(name = "evaluation_micros", nullable = false)
    private long evaluationMicros;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected EvaluationLogEntity() {
    }

    public EvaluationLogEntity(String txn, String decision, long version, List<Map<String, Object>> matches, Map<String, Object> request, long micros) {
        id = UUID.randomUUID();
        transactionId = txn;
        this.decision = decision;
        rulesetVersion = version;
        matchedRules = List.copyOf(matches);
        requestPayload = Map.copyOf(request);
        evaluationMicros = micros;
        createdAt = Instant.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getDecision() {
        return decision;
    }

    public long getRulesetVersion() {
        return rulesetVersion;
    }

    public List<Map<String, Object>> getMatchedRules() {
        return matchedRules;
    }

    public long getEvaluationMicros() {
        return evaluationMicros;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
