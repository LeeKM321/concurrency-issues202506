package com.playdata.concurrencyissues.repository;

import com.playdata.concurrencyissues.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProductId(Long productId);

    // Spring Data JPA에서 제공하는 Lock 아노테이션.
    // 비관적 락은 다른 사람(서버)이 내 데이터를 건들지 못하도록 문 잠가놓고 작업(sql)을 수행
    // SELECT * FROM stock WHERE id = 1 FOR UPDATE;
    // FOR UPDATE = 이 행을 내가 수정할 예정이니 잠가!
    // 락이 해제되는 시점은 트랜잭션이 끝나면 해제 (update가 완료된 후)
    // 비관적 락은 데이터 일관성 측면에서 굉장히 안전한 방법, 서버가 여러개여도 문제없이 동작
    // 성능 저하가 발생할 수 있음 (대기 시간), 동시 처리 능력도 좀 떨어짐
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.id = :id")
    Stock findByIdWithPessimisticLock(Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM Stock s WHERE s.id = :id")
    Stock findByIdWithOptimisticLock(Long id);

}








