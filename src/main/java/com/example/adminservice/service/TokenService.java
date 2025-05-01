package com.example.adminservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final long BLACKLIST_TTL = 24 * 60 * 60; // 1일 (초 단위)

    /**
     * 토큰을 블랙리스트에 추가합니다 (로그아웃)
     * @param token JWT 토큰
     */
    public void blacklistToken(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "true");
        redisTemplate.expire(key, BLACKLIST_TTL, TimeUnit.SECONDS);
        log.info("토큰이 블랙리스트에 추가되었습니다.");
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인합니다 (로그아웃된 토큰인지)
     * @param token JWT 토큰
     * @return 블랙리스트 포함 여부
     */
    public boolean isBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }
}