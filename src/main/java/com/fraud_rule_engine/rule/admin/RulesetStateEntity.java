package com.fraud_rule_engine.rule.admin;

import jakarta.persistence.*;

@Entity
@Table(name = "ruleset_state")
public class RulesetStateEntity {
    @Id
    private Integer id;
    @Column(nullable = false)
    private long version;

    protected RulesetStateEntity() {
    }

    public RulesetStateEntity(Integer id, long version) {
        this.id = id;
        this.version = version;
    }

    public long getVersion() {
        return version;
    }

    public void increment() {
        version++;
    }
}
