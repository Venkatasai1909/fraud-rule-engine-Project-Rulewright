package com.fraud_rule_engine.rule.feature;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "fraud.features.store", havingValue = "redis")
public class RedisFeatureStore implements FeatureStore {
    private static final DefaultRedisScript<Long> UPDATE = new DefaultRedisScript<>(
            "if redis.call('EXISTS',KEYS[2])==0 then redis.call('SET',KEYS[2],'1','EX',7200); redis.call('ZADD',KEYS[1],ARGV[1],ARGV[2]); end; " +
                    "redis.call('ZREMRANGEBYSCORE',KEYS[1],'-inf',ARGV[3]); redis.call('EXPIRE',KEYS[1],7200); " +
                    "return redis.call('ZCOUNT',KEYS[1],ARGV[4],'+inf')", Long.class);
    private final StringRedisTemplate redis;

    public RedisFeatureStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public Map<String, Object> recordAndGet(String customer, String txn, BigDecimal amount, Instant at) {
        long now = at.toEpochMilli();
        String key = "fraud:features:" + customer;
        Long count = redis.execute(UPDATE, List.of(key, "fraud:feature-dedup:" + customer + ":" + txn),
                Long.toString(now), txn + "|" + amount.toPlainString(), Long.toString(now - 3600000), Long.toString(now - 600000));
        var rows = redis.opsForZSet().rangeByScoreWithScores(key, now - 3600000, now);
        BigDecimal sum = BigDecimal.ZERO;
        if (rows != null) for (var row : rows) {
            String v = row.getValue();
            if (v != null) {
                int i = v.lastIndexOf('|');
                if (i >= 0) sum = sum.add(new BigDecimal(v.substring(i + 1)));
            }
        }
        return Map.of("customer.txnCount10m", count == null ? 0L : count, "customer.amountSum1h", sum);
    }
}
