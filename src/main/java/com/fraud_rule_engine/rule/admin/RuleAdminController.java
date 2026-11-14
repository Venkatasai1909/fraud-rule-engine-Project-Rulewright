package com.fraud_rule_engine.rule.admin;
import jakarta.validation.Valid; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/rules") public class RuleAdminController {
 private final RuleAdminService service; public RuleAdminController(RuleAdminService s){service=s;}
 @PostMapping public org.springframework.http.ResponseEntity<RuleDtos.VersionResponse> create(@Valid @RequestBody RuleDtos.CreateRuleRequest r){return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));}
 @PostMapping("/{ruleId}/versions") public org.springframework.http.ResponseEntity<RuleDtos.VersionResponse> version(@PathVariable UUID ruleId,@Valid @RequestBody RuleDtos.CreateVersionRequest r){return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(service.createVersion(ruleId,r));}
 @PostMapping("/{ruleId}/versions/{version}/activate") public RuleDtos.ActivationResponse activate(@PathVariable UUID ruleId,@PathVariable int version){return service.activate(ruleId,version);}
 @PostMapping("/{ruleId}/deactivate") public RuleDtos.ActivationResponse deactivate(@PathVariable UUID ruleId){return service.deactivate(ruleId);}
 @GetMapping public List<RuleDtos.RuleResponse> list(){return service.list();}
 @GetMapping("/{ruleId}/versions") public List<RuleDtos.VersionResponse> history(@PathVariable UUID ruleId){return service.history(ruleId);}
}
