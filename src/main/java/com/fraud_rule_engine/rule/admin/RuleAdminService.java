package com.fraud_rule_engine.rule.admin;

import com.fraud_rule_engine.rule.feature.RuleChangeNotifier;
import com.fraud_rule_engine.rule.rules.RuleCompiler;
import com.fraud_rule_engine.rule.rules.RuleParser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class RuleAdminService {
    private final RuleRepository rules;
    private final RuleVersionRepository versions;
    private final RulesetStateRepository state;
    private final RuleChangeNotifier notifier;
    private final RuleParser parser = RuleParser.standard();

    public RuleAdminService(RuleRepository r, RuleVersionRepository v, RulesetStateRepository s, RuleChangeNotifier notifier) {
        rules = r;
        versions = v;
        state = s;
        this.notifier = notifier;
    }

    @Transactional
    public RuleDtos.VersionResponse create(RuleDtos.CreateRuleRequest req) {
        validate(req.action(), req.condition());
        RuleEntity rule = rules.save(new RuleEntity(req.name()));
        RuleVersionEntity v = versions.save(new RuleVersionEntity(rule, 1, req.action().toUpperCase(Locale.ROOT), req.priority(), req.condition()));
        return dto(v);
    }

    @Transactional
    public RuleDtos.VersionResponse createVersion(UUID id, RuleDtos.CreateVersionRequest req) {
        state.lockState();
        validate(req.action(), req.condition());
        RuleEntity rule = requireRule(id);
        int n = versions.findTopByRule_IdOrderByVersionNoDesc(id).map(v -> v.getVersionNo() + 1).orElse(1);
        return dto(versions.save(new RuleVersionEntity(rule, n, req.action().toUpperCase(Locale.ROOT), req.priority(), req.condition())));
    }

    @Transactional
    public RuleDtos.ActivationResponse activate(UUID id, int version) {
        RulesetStateEntity s = state.lockState();
        RuleEntity rule = requireRule(id);
        RuleVersionEntity selected = versions.findByRule_IdOrderByVersionNoDesc(id).stream().filter(v -> v.getVersionNo() == version).findFirst().orElseThrow(() -> new EntityNotFoundException("Rule version not found: " + version));
        validate(selected.getAction(), selected.getCondition());
        versions.findByRule_IdAndStatus(id, RuleStatus.ACTIVE).ifPresent(old -> old.setStatus(RuleStatus.INACTIVE));
        selected.setStatus(RuleStatus.ACTIVE);
        s.increment();
        publishAfterCommit(s.getVersion());
        return new RuleDtos.ActivationResponse(id, version, s.getVersion(), "ACTIVE");
    }

    @Transactional
    public RuleDtos.ActivationResponse deactivate(UUID id) {
        RulesetStateEntity s = state.lockState();
        requireRule(id);
        versions.findByRule_IdAndStatus(id, RuleStatus.ACTIVE).ifPresent(v -> v.setStatus(RuleStatus.INACTIVE));
        s.increment();
        publishAfterCommit(s.getVersion());
        return new RuleDtos.ActivationResponse(id, 0, s.getVersion(), "INACTIVE");
    }

    @Transactional(readOnly = true)
    public List<RuleDtos.RuleResponse> list() {
        return rules.findAll().stream().map(r -> {
            List<RuleVersionEntity> vs = versions.findByRule_IdOrderByVersionNoDesc(r.getId());
            RuleVersionEntity a = vs.stream().filter(v -> v.getStatus() == RuleStatus.ACTIVE).findFirst().orElse(null);
            String current = a != null ? "ACTIVE" : (vs.stream().anyMatch(v -> v.getStatus() == RuleStatus.INACTIVE) ? "INACTIVE" : "DRAFT");
            return new RuleDtos.RuleResponse(r.getId(), r.getName(), current, a == null ? null : a.getVersionNo(), r.getCreatedAt());
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<RuleDtos.VersionResponse> history(UUID id) {
        requireRule(id);
        return versions.findByRule_IdOrderByVersionNoDesc(id).stream().map(this::dto).toList();
    }

    private void publishAfterCommit(long version) {
        Runnable publish = () -> notifier.publish(version);
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
        else publish.run();
    }

    private void validate(String action, Map<String, Object> condition) {
        try {
            com.fraud_rule_engine.rule.engine.Action.valueOf(action.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new IllegalArgumentException("action must be BLOCK or REVIEW");
        }
        RuleCompiler.compile(parser.parse(condition));
    }

    private RuleEntity requireRule(UUID id) {
        return rules.findById(id).orElseThrow(() -> new EntityNotFoundException("Rule not found: " + id));
    }

    private RuleDtos.VersionResponse dto(RuleVersionEntity v) {
        return new RuleDtos.VersionResponse(v.getRule().getId(), v.getRule().getName(), v.getVersionNo(), v.getStatus().name(), v.getAction(), v.getPriority(), v.getCondition(), v.getCreatedAt());
    }
}
