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
    public void decreaseStock(Long id, Long quantity) {
        // 1. 재고 조회
        Stock stock = stockRepository.findByProductId(id).orElseThrow(
                () -> new EntityNotFoundException("Product not found")
        );

        // 2. 재고 감소
        stock.decreaseQuantity(quantity);

        // 3. 저장
        stockRepository.save(stock);
    }



}









