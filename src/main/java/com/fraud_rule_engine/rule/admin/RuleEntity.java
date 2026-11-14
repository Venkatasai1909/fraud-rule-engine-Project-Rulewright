package com.fraud_rule_engine.rule.admin;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="fraud_rule") public class RuleEntity {
 @Id private UUID id; @Column(nullable=false,length=120) private String name; @Column(nullable=false) private Instant createdAt;
 protected RuleEntity(){} public RuleEntity(String name){this.id=UUID.randomUUID();this.name=name;this.createdAt=Instant.now();}
 public UUID getId(){return id;} public String getName(){return name;} public Instant getCreatedAt(){return createdAt;}
}
