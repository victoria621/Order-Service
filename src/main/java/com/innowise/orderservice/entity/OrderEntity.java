package com.innowise.orderservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_deleted", columnList = "deleted")
})
@SQLDelete(sql = "UPDATE orders SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class OrderEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    public OrderEntity() {}

    public OrderEntity(Long userId, OrderStatus status, BigDecimal totalPrice) {
        this.userId = userId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.deleted = false;
    }


    public void addOrderItem(ItemEntity item, Integer quantity) {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setItem(item);
        orderItem.setQuantity(quantity);
        orderItem.setOrder(this);
        this.orderItems.add(orderItem);
    }

    public void removeOrderItem(OrderItemEntity orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = OrderStatus.NEW;
        }
    }

}