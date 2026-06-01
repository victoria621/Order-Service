package com.innowise.orderservice.repository;

import com.innowise.orderservice.entity.OrderEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity,Long>,
        JpaSpecificationExecutor<OrderEntity> {

    List<OrderEntity> findByUserId(Long id);

    List<OrderEntity> findByUserId(Long id, Pageable pageable);
}
