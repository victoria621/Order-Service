package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.CreateOrderRequest;
import com.innowise.orderservice.dto.OrderResponse;
import com.innowise.orderservice.dto.UpdateOrderRequest;
import com.innowise.orderservice.entity.OrderStatus;
import com.innowise.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {
        log.info("Create Order");
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(createOrderRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id){
        log.info("Get order by id: {}", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrderWithFilters(Pageable pageable,
                                                                   @RequestParam(required = false) LocalDateTime fromDate,
                                                                   @RequestParam(required = false) LocalDateTime toDate,
                                                                   @RequestParam(required = false) List<OrderStatus> statuses){
        log.info("get order  with statuses: {}", statuses);
        return ResponseEntity.ok(orderService.getOrdersWithFilters(pageable, fromDate, toDate, statuses));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByUserId(@PathVariable Long userId, Pageable pageable){
        log.info("Get orders by user id: {}", userId);
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id,@Valid @RequestBody UpdateOrderRequest updateOrderRequest) {
        log.info("update order for id: {}", id);
        return ResponseEntity.ok(orderService.updateOrder(id, updateOrderRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
        log.info("delete order for id: {}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }


}
