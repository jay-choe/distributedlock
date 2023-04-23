package com.example.distributedlock.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.embedded.RedisServer;

@TestConfiguration
public class RedisConfig {

    public static int bindingPort = 6379;
    private final RedisServer redisServer;

    public RedisConfig() {
        redisServer = new RedisServer(bindingPort);
    }


    public RedisTemplate<?, ?> redisTemplate() {
        final var redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.string());

        return redisTemplate;
    }

    @PostConstruct
    void init() {
        System.out.println("Redis Server starts");
        redisServer.start();
    }

    @PreDestroy
    void tearDown() {
        redisServer.stop();
    }
}
