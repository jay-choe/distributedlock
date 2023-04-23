package com.example.distributedlock.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LettuceLockService {

    private final RedisTemplate<String, String> redisTemplate;
    private final StockService stockService;

    public boolean lock(long key) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
            .setIfAbsent(key + "", "lock", Duration.ofSeconds(2)));
    }

    public void unlock(long key) {
        redisTemplate.delete(key + "");
    }

    public void increaseStock(long key) {

        while (!lock(key)) {
            // Atomic operation
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                // Do Nothing
            }
        }
        // Increase
        try {
            stockService.increaseStockFrom(key, 1);
        } finally {
            unlock(key);
        }
    }
}
