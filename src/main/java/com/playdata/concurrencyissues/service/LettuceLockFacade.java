package com.playdata.concurrencyissues.service;

import com.playdata.concurrencyissues.repository.RedisLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class LettuceLockFacade {

    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public void decrease(Long id, Long quantity) throws InterruptedException {
        String lockValue = UUID.randomUUID().toString(); // 고유 식별자
        boolean lockAcquired = false; // 락 획득 여부

        try {
            // 락 획득을 시도해서 획득 실패 시 0.1초 쉬었다가 다시 획득 시도.
            while (!redisLockRepository.tryLock(id, lockValue)) {
                log.info("lock 획득 실패!");
                Thread.sleep(100);
            }

            log.info("lock 획득!");
            lockAcquired = true;
            // 실제 비즈니스 로직
            stockService.decreaseStock(id, quantity);
        } catch (Exception e) {
            log.info("재고 감소 로직 과정에서 문제 발생!");
            throw new RuntimeException(e);
        } finally {
            // 예외 발생 여부와 상관 없이 반드시 락 해제
            if (lockAcquired) {
                boolean released = redisLockRepository.unlock(id);// 락 해제
                if (released) {
                    log.info("lock 해제!");
                } else {
                    log.info("lock 해제 실패! (이미 만료되었거나 다른 프로세스)");
                }
            }

        }

    }

}










