package com.fraud_rule_engine.rule.propagation;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RuleSetStartup implements ApplicationRunner {
    private final RuleSetRefreshService refresh;

    public RuleSetStartup(RuleSetRefreshService r) {
        refresh = r;
    }

    public void run(ApplicationArguments args) {
        refresh.refresh();
    }
}
