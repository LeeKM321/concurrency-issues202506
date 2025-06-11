package com.playdata.concurrencyissues.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

// 낙관적 락 재시도를 담당하는 로직
// 파사드 패턴: 복잡한 로직은 숨기고 간단하게 포장한 패턴을 외부에 공개해서
// 사용하는 입장에서도 쉽게 호출하고, 유지 보수 측면에서도 간편한 패턴. (내부 로직이 변해도 외부는 변하지 않음)
@Component
@Slf4j
@RequiredArgsConstructor
public class OptimisticLockFacade {

    private final OptimisticLockStockService optimisticLockStockService;

    public void decrease(Long id, Long stockQuantity) throws InterruptedException {
        while (true) {
            try {
                optimisticLockStockService.decreaseStock(id, stockQuantity);

                // 재고 감소 업데이트를 성공했다면 반복문 종료
                break;
            } catch (ObjectOptimisticLockingFailureException e) {
                // 버전 정보를 가지고 DB에 접근했는데, 버전이 일치하지 않아서 업데이트에 실패하면 발생하는 예외.
                log.error("업데이트 실패!: {}", e.getMessage());
                Thread.sleep(100);
            }
        }
    }

}









