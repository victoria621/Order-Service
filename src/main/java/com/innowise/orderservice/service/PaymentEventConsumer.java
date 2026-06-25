package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.PaymentEvent;
import com.innowise.orderservice.dto.PaymentStatus;
import com.innowise.orderservice.entity.OrderEntity;
import com.innowise.orderservice.entity.OrderStatus;
import com.innowise.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "payment-events")
    public void process(PaymentEvent paymentEvent) {
        log.info("Received Payment Event: {}", paymentEvent);
        OrderEntity order = orderRepository.findById(paymentEvent.getOrderId()).orElseThrow(
                () -> new RuntimeException("Order not found")
        );
        PaymentStatus status = paymentEvent.getStatus();
        if(status == PaymentStatus.SUCCESS){
            order.setStatus(OrderStatus.COMPLETED);
        }
        if(status == PaymentStatus.FAILED){
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);
    }
}
