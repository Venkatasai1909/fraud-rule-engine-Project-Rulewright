package com.fraud_rule_engine.rule.admin;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "rule_version", uniqueConstraints = @UniqueConstraint(columnNames = {"rule_id", "version_no"}))
public class RuleVersionEntity {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id")
    private RuleEntity rule;
    @Column(name = "version_no", nullable = false)
    private int versionNo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RuleStatus status;
    @Column(nullable = false, length = 16)
    private String action;
    @Column(nullable = false)
    private int priority;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> condition;
    @Column(nullable = false)
    private Instant createdAt;

    protected RuleVersionEntity() {
    }

    public RuleVersionEntity(RuleEntity rule, int version, String action, int priority, Map<String, Object> condition) {
        this.id = UUID.randomUUID();
        this.rule = rule;
        this.versionNo = version;
        this.action = action;
        this.priority = priority;
        this.condition = Map.copyOf(condition);
        this.status = RuleStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public RuleEntity getRule() {
        return rule;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public RuleStatus getStatus() {
        return status;
    }

    public void setStatus(RuleStatus s) {
        status = s;
    }

    public String getAction() {
        return action;
    }

    public int getPriority() {
        return priority;
    }

    public Map<String, Object> getCondition() {
        return condition;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
