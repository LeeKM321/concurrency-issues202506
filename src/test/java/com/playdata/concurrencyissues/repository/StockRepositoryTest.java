package com.playdata.concurrencyissues.repository;

import com.playdata.concurrencyissues.entity.Stock;
import com.playdata.concurrencyissues.service.LettuceLockFacade;
import com.playdata.concurrencyissues.service.OptimisticLockFacade;
import com.playdata.concurrencyissues.service.PessimisticLockStockService;
import com.playdata.concurrencyissues.service.StockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockRepositoryTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private PessimisticLockStockService pessimisticLockStockService;

    @Autowired
    private OptimisticLockFacade optimisticLockFacade;

    @Autowired
    private LettuceLockFacade lettuceLockFacade;

    @Autowired
    private StockRepository stockRepository;

    // 각 테스트가 실행되기 전 이 메서드를 먼저 실행하라!
    @BeforeEach
    public void setup() {
        stockRepository.save(new Stock(1L, 1L, 100L));
    }

    // 각 테스트가 끝난 후 이 메서드 호출!
    @AfterEach
    public void tearDown() {
        stockRepository.deleteAll();
    }

    @Test
    @DisplayName("완전 평범한 재고 감소 로직")
    void simpleDecreaseTest() {
        stockService.decreaseStock(1L, 1L);

        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(99L, stock.getQuantity());
    }
    
    @Test
    @DisplayName("동시성 문제 확인 - 100명이 1개씩 구매")
    void 동시에_100개의_주문_요청() throws InterruptedException {
        int threadCount = 100;

        // 비동기로 실행하는 작업을 간단하게 수행할 수 있게 도와주는 ExecutorService
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 100개 작업이 모두 끝날 때 까지 기다리는 역할
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 100번의 작업 요청
        for (int i = 0; i < threadCount; i++) {
            // 실행하고자 하는 작업을 스레드에 제출하는 메서드
            executorService.submit(() -> {
                try {
                    lettuceLockFacade.decrease(1L, 1L);
                } catch (Exception e) {
                    System.out.println("구매 실패: " + e.getMessage());
                } finally {
                    // 카운트다운 1개 감소
                    latch.countDown();
                }
            });
        }

        // 재고 확인을 위한 로직을 밑에다가 쓸 건데, 카운트가 0이 될 때까지 다음 코드를 실행하지 않게 해 주는 역할.
        latch.await();

        // 100번의 요청 이후 재고를 확인해 보자.
        // 100번의 재고 감소 요청을 넣었으니, 당연히 재고는 0이 아닐까?
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0, stock.getQuantity());


    }

    @Test
    @DisplayName("서버 2대 상황 시뮬레이션")
    void multiServerTest() throws InterruptedException {

        // 서버 A
        ExecutorService serverA = Executors.newFixedThreadPool(25);

        // 서버 B
        ExecutorService serverB = Executors.newFixedThreadPool(25);

        CountDownLatch latch = new CountDownLatch(100);

        // 서버 A 고객들
        for (int i = 0; i < 50; i++) {
            final int customerId = i;
            serverA.submit(() -> {
                try {
                    System.out.println("서버 A 고객 " + customerId + "주문 시작");
                    stockService.decreaseStock(1L, 1L);
                    System.out.println("서버 A 고객 " + customerId + "주문 완료!");
                } catch (Exception e) {
                    System.out.println("서버 A 고객 " + customerId + "주문 실패!");
                } finally {
                    latch.countDown();
                }
            });
        }

        // 서버 B 고객들
        for (int i = 0; i < 50; i++) {
            final int customerId = i;
            serverB.submit(() -> {
                try {
                    System.out.println("서버 B 고객 " + customerId + "주문 시작");
                    stockService.decreaseStock(1L, 1L);
                    System.out.println("서버 B 고객 " + customerId + "주문 완료!");
                } catch (Exception e) {
                    System.out.println("서버 B 고객 " + customerId + "주문 실패!");
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 결과 확인
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0, stock.getQuantity());


    }

}












