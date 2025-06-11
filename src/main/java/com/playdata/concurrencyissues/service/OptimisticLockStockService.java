package com.playdata.concurrencyissues.service;

import com.playdata.concurrencyissues.entity.Stock;
import com.playdata.concurrencyissues.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OptimisticLockStockService {

    private final StockRepository stockRepository;

    // DB 락을 사용하기 때문에 synchronized 필요 없어요.
    public void decreaseStock(Long id, Long quantity) {
        // 1. 재고 조회 (비관적 락 메서드 호출)
        Stock stock = stockRepository.findByIdWithOptimisticLock(id);

        // 2. 재고 감소
        stock.decreaseQuantity(quantity);

        // 3. 저장
        stockRepository.save(stock);
    }


}










