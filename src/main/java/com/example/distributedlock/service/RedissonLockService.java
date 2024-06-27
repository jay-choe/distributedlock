package com.example.distributedlock.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedissonLockService {

    private final StockService stockService;
    private final RedissonClient redissonClient;

    private static final int WAIT_TIME = 5;
    private static final int OCCUPATION_TIME = 1;
    public void increaseStock(long key) {
        RLock lock = redissonClient.getLock(key + "");

        try {
            boolean success = lock.tryLock(WAIT_TIME, OCCUPATION_TIME, TimeUnit.SECONDS);

            if (!success) {
                // retry -> just test code.. (recursion)
                increaseStock(key);
            }

            stockService.increaseStockFrom(key, 1);

        } catch (Exception e) {
            // Do Nothing
        } finally {
            lock.unlock();
        }

    }
}
