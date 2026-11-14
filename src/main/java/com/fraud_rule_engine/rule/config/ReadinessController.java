package com.fraud_rule_engine.rule.config;

import com.fraud_rule_engine.rule.engine.SnapshotHolder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ReadinessController {
    private final SnapshotHolder snapshots;

    public ReadinessController(SnapshotHolder snapshots) {
        this.snapshots = snapshots;
    }

    @GetMapping("/internal/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        if (!snapshots.isLoaded())
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("ready", false, "reason", "No ruleset loaded"));
        return ResponseEntity.ok(Map.of("ready", true, "rulesetVersion", snapshots.current().revision(), "activeRules", snapshots.current().rules().size()));
    }
}
