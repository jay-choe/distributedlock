package com.example.distributedlock;

import com.example.distributedlock.config.RedisConfig;
import com.example.distributedlock.entity.Stock;
import com.example.distributedlock.repository.StockRepository;
import java.util.Objects;
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
}
