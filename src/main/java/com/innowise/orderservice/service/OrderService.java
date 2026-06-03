package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.entity.ItemEntity;
import com.innowise.orderservice.entity.OrderEntity;
import com.innowise.orderservice.entity.OrderItemEntity;
import com.innowise.orderservice.entity.OrderStatus;
import com.innowise.orderservice.mapper.OrderItemMapper;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.specification.OrderSpecifications;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ItemService itemService;
    private final UserClient userClient;
    private final OrderItemMapper orderItemMapper;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper,
                        ItemService itemService, UserClient userClient, OrderItemMapper orderItemMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.itemService = itemService;
        this.userClient = userClient;
        this.orderItemMapper = orderItemMapper;
    }

    private OrderResponse buildOrderResponse(OrderEntity order, UserInfoResponse userInfo) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(orderItemMapper::toDto)
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalPrice(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                userInfo
        );
    }

    @Transactional
    public OrderResponse updateOrder(Long id, UpdateOrderRequest updateOrderRequest) {
        OrderEntity orderEntity = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Order with id: " + id + " not found")
        );

        if (updateOrderRequest.status() != null) {
            orderEntity.setStatus(updateOrderRequest.status());
        }

        if (updateOrderRequest.items() != null) {
            updateOrderItems(orderEntity, updateOrderRequest.items());
        }

        OrderEntity updated = orderRepository.save(orderEntity);
        UserInfoResponse userInfoResponse= userClient.getUserById(updated.getUserId());
        return buildOrderResponse(updated, userInfoResponse);
    }

    private void updateOrderItems(OrderEntity orderEntity, @Valid List<OrderItemRequest> items) {
        orderEntity.getOrderItems().clear();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for(OrderItemRequest itemRequest : items) {
            ItemEntity entity = itemService.findById(itemRequest.itemId());

            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrder(orderEntity);
            orderItemEntity.setItemId(entity.getId());
            orderItemEntity.setQuantity(itemRequest.quantity());

            orderEntity.getOrderItems().add(orderItemEntity);

            BigDecimal itemTotal = entity.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            totalPrice = totalPrice.add(itemTotal);
        }

        orderEntity.setTotalPrice(totalPrice);

    }

    public OrderResponse getOrderById(Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order with id: " + id + " not found"));

        UserInfoResponse userInfoResponse= userClient.getUserById(order.getUserId());
        return buildOrderResponse(order, userInfoResponse);
    }

    @Transactional
    public void deleteOrder(Long id) {
        OrderEntity order = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Order with id: " + id + " not found")
        );
        order.setDeleted(true);
        orderRepository.save(order);
    }

    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {

        Page<OrderEntity> ordersPage = orderRepository.findByUserId(userId, pageable);

        UserInfoResponse userInfoResponse = userClient.getUserById(userId);

        List<OrderResponse> orderResponses = ordersPage.getContent().stream()
                .map(order -> buildOrderResponse(order, userInfoResponse))
                .collect(Collectors.toList());

        return new PageImpl<>(orderResponses, pageable, ordersPage.getTotalElements());
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        OrderEntity orderEntity = orderMapper.toEntity(createOrderRequest);
        orderEntity.setDeleted(false);

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : createOrderRequest.items()) {
            ItemEntity item = itemService.findById(itemRequest.itemId());

            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrder(orderEntity);
            orderItemEntity.setItemId(item.getId());
            orderItemEntity.setQuantity(itemRequest.quantity());

            orderEntity.getOrderItems().add(orderItemEntity);

            totalPrice = totalPrice.add(item.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }
        orderEntity.setTotalPrice(totalPrice);

        OrderEntity saved = orderRepository.save(orderEntity);
        UserInfoResponse userInfoResponse= userClient.getUserById(saved.getUserId());
        return buildOrderResponse(saved, userInfoResponse);
    }

    public Page<OrderResponse> getOrdersWithFilters(Pageable pageable,
                                                    LocalDateTime fromDate,
                                                    LocalDateTime toDate,
                                                    List<OrderStatus> statuses) {

        Specification<OrderEntity> spec = Specification.allOf(
                OrderSpecifications.createdBetween(fromDate, toDate),
                OrderSpecifications.hasStatusIn(statuses)
        );

        Page<OrderEntity> ordersPage = orderRepository.findAll(spec, pageable);

        if (ordersPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<OrderResponse> orderResponses = ordersPage.getContent().stream()
                .map(order -> {
                    UserInfoResponse userInfo = userClient.getUserById(order.getUserId());
                    return buildOrderResponse(order, userInfo);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(orderResponses, pageable, ordersPage.getTotalElements());
    }

}
