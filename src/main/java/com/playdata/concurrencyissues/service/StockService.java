package com.playdata.concurrencyissues.service;

import com.playdata.concurrencyissues.entity.Stock;
import com.playdata.concurrencyissues.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    // 주문이 들어오면 재고를 감소시키는 메서드
    // synchronized: 한 개의 스레드만 접근이 가능하도록 제어하는 자바의 키워드
    // synchronized 없을 때 -> 여러 스레드가 동시에 메서드를 호출 -> 재고 수량 파악에서 중복
    public synchronized void decreaseStock(Long id, Long quantity) {
        // 1. 재고 조회
        Stock stock = stockRepository.findByProductId(id).orElseThrow(
                () -> new EntityNotFoundException("Product not found")
        );

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 2. 재고 감소
        stock.decreaseQuantity(quantity);

        // 3. 저장
        stockRepository.save(stock);
    }



}









