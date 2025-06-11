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

    // 낙관적 락: 데이터에 버전 번호를 붙여서 누군가가 먼저 바꿨는지 확인하는 방법
    // 데이터 수정 시 버전이 일치하지 않는 경우 -> 예외 발생 -> 수정을 재요청
    // 낙관적 락 또한 서버가 여러 개여도 문제없이 동작 (DB에 락을 설정)
    // 비관적 락보다 성능적인 측면에서 이점 (무조건은 x)
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM Stock s WHERE s.id = :id")
    Stock findByIdWithOptimisticLock(Long id);

    /*
    낙관적 락 vs 비관적 락

    1. 대부분의 일반적인 상황에서는 낙관적 락 우선시 (응답 속도가 중요할 시, 쿼리 충돌률이 적은 경우, 읽기가 주이고 쓰기가 적은 경우)
    2. 데이터 정확성이 절대적으로 중요, 충돌이 자주 발생하는 경우에는 비관적 락을 선호
        충돌률 낮은 경우(조회수 업데이트, 일반 상품 구매 등)
        충돌률 높은 경우(인기가수 티켓 예매, 선착순 쿠폰 등)

    낙관적 락 & 비관적 락은 DB에 직접 Lock을 거는 방식이기 때문에
    DB 성능이 떨어진다면 과부하가 발생하고, 장애가 발생할 가능성이 크다...
    -> 다른 도구(redis)를 이용한 Lock 방식 최근 선호.
     */

}








