package com.example.distributedlock;

import com.example.distributedlock.config.RedisConfig;
import com.example.distributedlock.entity.Stock;
import com.example.distributedlock.repository.StockRepository;
import com.example.distributedlock.service.StockService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

@SpringBootTest(classes = RedisConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class DistributedlockApplicationTests {

    @Autowired
    StockService stockService;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    RedisTemplate<?, ?> redisTemplate;

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.26")
        .withUsername("testuser")
        .withPassword("testpass")
        .withDatabaseName("testdb");


    @BeforeAll
    static void init() {
        mysqlContainer.start();
    }

    @AfterEach
    void tearDown() {
        mysqlContainer.stop();
    }

    @Test
    @DisplayName("Test for initializing")
    void initTest() {

        /**
         * Test For RDS Connection
         */

        Stock stock = Stock.builder()
            .name("stock1")
            .quantity(1L)
            .build();

        Stock stock1 = stockRepository.saveAndFlush(stock);

        Assertions.assertNotNull(stock1.getId());

        stockRepository.delete(stock1);

        /**
         * Test For Redis Connection
         */

        String result = redisTemplate.getConnectionFactory()
            .getConnection()
            .ping();

        Assertions.assertEquals("PONG", result);

    }

    @Test
    @DisplayName("Concurrency Issue Test")
    void concurrency_issue_test() throws InterruptedException {

        final int testUpdateRequestCount = 1000;

        final CountDownLatch countDownLatch = new CountDownLatch(1000);

        ExecutorService executorService = Executors.newFixedThreadPool(32);

        final long startCount = 1L;

        final Stock toSave = Stock.builder()
            .name("testStock")
            .quantity(startCount)
            .build();

        final Stock savedStock = stockRepository.saveAndFlush(toSave);

        List<Callable<Void>> taskList = new ArrayList<>(testUpdateRequestCount);

        for (int i = 0; i < testUpdateRequestCount; i++) {
            taskList.add(() -> {
                stockService.increaseStockFrom(savedStock.getId(), 1);
                countDownLatch.countDown();
                return null;
            });
        }

        executorService.invokeAll(taskList);
        countDownLatch.await();

        Assertions.assertFalse(stockRepository.findById(savedStock.getId())
            .get().getQuantity() != testUpdateRequestCount + startCount, "업데이트 횟수만큼 재고가 증가되지 않았다");

        executorService.shutdown();
    }
}
