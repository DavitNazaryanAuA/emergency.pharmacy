package com.capstone.emergency.pharmacy.core.auth.jwt.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Repository
public class TokenRedisRepository {

    @Value("${redis.machine.lock.prefix}")
    private String jwtBlacklistPrefix;

    @Value("${jwt.access.exp.minutes}")
    private Integer accessTokenExpMinutes;

    private final ValueOperations<String, Object> valueOperations;


    public void blacklistAccessToken(String token) {
        final var key = jwtBlacklistPrefix + ":" + token;
        valueOperations.set(key, 1);
        valueOperations.getOperations().expire(key, accessTokenExpMinutes, TimeUnit.MINUTES);
    }

    public Boolean isBlackListed(String token) {
        final var key = jwtBlacklistPrefix + ":" + token;
        final var existing = valueOperations.get(key);
        return existing != null;
    }
}
