package com.playdata.concurrencyissues.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedissonLockFacade {

    private final RedissonClient redissonClient;
    private final StockService stockService;

    public void decrease(Long id, Long quantity) {
        // 락 객체를 생성 (아직 락 획득은 아님)
        RLock lock = redissonClient.getLock("stock:lock:" + id);
        boolean lockAcquired = false; // 락 획득 여부

        try {
            lockAcquired = lock.tryLock(
                    10, // 락이 이미 점유되었다면 최대 10초동안 락을 기다림.
                    5, // 락을 점유했다면 5초동안 유지, 5초 후에는 자동 락 해제 (데드락 방지)
                    TimeUnit.SECONDS
            );

            if (!lockAcquired) {
                log.warn("락 획득 실패!: {}", id);
                return;
            }

            log.info("락 획득 성공!: {}", id);
            // 비즈니스 로직 실행
            stockService.decreaseStock(id, quantity);
            log.info("재고 감소 완료!");
        } catch (Exception e) {
            log.error("락 획득 중 인터럽트 발생, 재고 처리 과정에서 문제 발생!");
        } finally {
            if (lockAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("락 해제 완료!");
            }
        }

    }


}









