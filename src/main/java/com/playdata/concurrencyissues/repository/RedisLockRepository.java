package com.playdata.concurrencyissues.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisLockRepository {

    // 기존에는 우리가 RedisConfig에서 직접 ConnectionFactory와 Template 객체를 세팅하고 빈 등록.
    // 지금은 테스트 환경이니까 가장 기본 형태의 redisTemplate을 사용하려고 함.
    // host가 localhost고 포트번호가 6379로 고정. 데이터 형태도 <String, String>으로 고정.
    private final RedisTemplate<String, String> redisTemplate;

    // Redis Lock 로직 구현
    public boolean tryLock(Long key, String lockValue) {
        return redisTemplate.opsForValue()
                .setIfAbsent(
                        key.toString(), // key
                        lockValue,
                        Duration.ofMillis(3_000)
                );
    }

    // Redis Lock 해제 메서드
    public boolean unlock(Long key) {
        return redisTemplate.delete(key.toString());
    }

}







