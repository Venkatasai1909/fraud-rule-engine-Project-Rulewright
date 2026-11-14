package com.fraud_rule_engine.rule.api;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.Instant;
public record EvaluationRequest(@NotBlank @Size(max=160) String transactionId,@NotBlank @Size(max=160) String customerId,@NotNull @DecimalMin("0.0") BigDecimal amount,@NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency,@NotBlank @Pattern(regexp="POS|ECOM|ATM|UPI") String channel,@NotBlank @Pattern(regexp="[0-9]{4}") String merchantCategory,@NotBlank @Size(min=2,max=3) String country,@NotNull Boolean newDevice,@NotNull Instant timestamp){}
