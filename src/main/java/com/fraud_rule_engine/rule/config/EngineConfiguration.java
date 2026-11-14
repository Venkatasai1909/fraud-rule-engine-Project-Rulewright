package com.fraud_rule_engine.rule.config;

import com.fraud_rule_engine.rule.engine.SnapshotHolder;
import org.springframework.context.annotation.*;

@Configuration
public class EngineConfiguration {
    @Bean
    public SnapshotHolder snapshotHolder() {
        return new SnapshotHolder();
    }
}
