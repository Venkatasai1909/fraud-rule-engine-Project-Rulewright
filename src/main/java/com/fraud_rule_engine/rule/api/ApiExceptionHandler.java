package com.fraud_rule_engine.rule.api;
import com.fraud_rule_engine.rule.rules.RuleValidationException; import jakarta.persistence.EntityNotFoundException; import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(RuleValidationException.class) ResponseEntity<Map<String,Object>> validation(RuleValidationException e){return ResponseEntity.badRequest().body(Map.of("error","Invalid condition","errors",e.errors()));}
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<Map<String,Object>> bad(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,Object>> invalid(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("error","Request validation failed","errors",e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).toList()));}
 @ExceptionHandler(EntityNotFoundException.class) ResponseEntity<Map<String,Object>> missing(EntityNotFoundException e){return ResponseEntity.status(404).body(Map.of("error",e.getMessage()));}
 @ExceptionHandler(IllegalStateException.class) ResponseEntity<Map<String,Object>> state(IllegalStateException e){return ResponseEntity.status(503).body(Map.of("error",e.getMessage()));}
}
