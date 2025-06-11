package com.playdata.concurrencyissues.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Stock {

    @Id
    private Long id;

    private Long productId;
    private Long quantity;

    @Version
    private Long version; // 낙관적 락을 위한 버전 정보

    public Stock(Long id, Long productId, Long quantity) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
    }

    public void decreaseQuantity(Long quantity){
        if (this.quantity - quantity < 0) {
            throw new RuntimeException("재고는 0 미만이 될 수 없어요!");
        }
        this.quantity -= quantity;
    }

}








