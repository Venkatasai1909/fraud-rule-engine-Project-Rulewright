package com.fraud_rule_engine.rule.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class EvaluationController {
    private final EvaluationService service;

    public EvaluationController(EvaluationService s) {
        service = s;
    }

    @PostMapping("/evaluate")
    public EvaluationResponse evaluate(@Valid @RequestBody EvaluationRequest r) {
        return service.evaluate(r);
    }

    @GetMapping("/evaluations/{transactionId}")
    public EvaluationResponse get(@PathVariable String transactionId) {
        return service.get(transactionId);
    }
}
