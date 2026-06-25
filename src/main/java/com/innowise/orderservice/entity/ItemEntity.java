package com.innowise.orderservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "items", indexes = {
        @Index(name = "idx_items_name", columnList = "name")
})
@Getter
@Setter
public class ItemEntity extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    public ItemEntity() {}

    public ItemEntity(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

}
