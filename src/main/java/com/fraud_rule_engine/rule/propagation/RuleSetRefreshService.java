package com.fraud_rule_engine.rule.propagation;
import com.fraud_rule_engine.rule.engine.SnapshotHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
public class RuleSetRefreshService {
 private final RuleSetLoader loader; private final SnapshotHolder holder;
 public RuleSetRefreshService(RuleSetLoader loader,SnapshotHolder holder){this.loader=loader;this.holder=holder;}
 @Scheduled(fixedDelayString="${fraud.rules.refresh-interval-ms:1000}")
 public void refresh(){
  long databaseVersion=loader.currentVersion();
  if(databaseVersion>holder.current().revision()) holder.swapIfNewer(loader.load());
 }
}
