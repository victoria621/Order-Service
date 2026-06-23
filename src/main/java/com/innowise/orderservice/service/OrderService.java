package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.entity.ItemEntity;
import com.innowise.orderservice.entity.OrderEntity;
import com.innowise.orderservice.entity.OrderItemEntity;
import com.innowise.orderservice.entity.OrderStatus;
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

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper,
                        ItemService itemService, UserClient userClient) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.itemService = itemService;
        this.userClient = userClient;
    }


    private OrderResponse enrichWithUserInfo(OrderResponse response, UserInfoResponse userInfo) {
        if (response == null) {
            return null;
        }

        List<OrderItemResponse> itemsWithSubtotal = response.items().stream()
                .map(item -> {
                    BigDecimal subtotal = BigDecimal.ZERO;
                    if (item.itemPrice() != null && item.quantity() != null) {
                        subtotal = item.itemPrice().multiply(BigDecimal.valueOf(item.quantity()));
                    }
                    return new OrderItemResponse(
                            item.id(),
                            item.itemId(),
                            item.itemName(),
                            item.itemPrice(),
                            item.quantity(),
                            subtotal
                    );
                })
                .collect(Collectors.toList());

        UserInfoResponse safeUserInfo = userInfo != null
                ? userInfo
                : new UserInfoResponse(
                response.userId(),
                "unknown@email.com",
                "Unknown",
                "User",
                false
        );

        return new OrderResponse(
                response.id(),
                response.userId(),
                response.status(),
                response.totalPrice(),
                response.deleted(),
                itemsWithSubtotal,
                response.createdAt(),
                response.updatedAt(),
                safeUserInfo
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

        OrderResponse response = orderMapper.toResponse(updated);
        return enrichWithUserInfo(response, userInfoResponse);
    }

    private void updateOrderItems(OrderEntity orderEntity, @Valid List<OrderItemRequest> items) {
        orderEntity.getOrderItems().clear();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for(OrderItemRequest itemRequest : items) {
            ItemEntity entity = itemService.findById(itemRequest.itemId());

            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrder(orderEntity);
            orderItemEntity.setItem(entity);
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
        OrderResponse response = orderMapper.toResponse(order);
        return enrichWithUserInfo(response, userInfoResponse);
    }

    @Transactional
    public void deleteOrder(Long id) {
        OrderEntity order = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Order with id: " + id + " not found")
        );
        orderRepository.delete(order);
    }

    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {

        Page<OrderEntity> ordersPage = orderRepository.findByUserId(userId, pageable);

        UserInfoResponse userInfoResponse = userClient.getUserById(userId);

        List<OrderResponse> orderResponses = ordersPage.getContent().stream()
                .map(order -> {
                    OrderResponse response = orderMapper.toResponse(order);
                    return enrichWithUserInfo(response, userInfoResponse);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(orderResponses, pageable, ordersPage.getTotalElements());
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        OrderEntity orderEntity = orderMapper.toEntity(createOrderRequest);
        orderEntity.setDeleted(false);
        orderEntity.setStatus(OrderStatus.NEW);

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : createOrderRequest.items()) {
            ItemEntity item = itemService.findById(itemRequest.itemId());

            orderEntity.addOrderItem(item, itemRequest.quantity());

            totalPrice = totalPrice.add(item.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }
        orderEntity.setTotalPrice(totalPrice);

        OrderEntity saved = orderRepository.save(orderEntity);
        UserInfoResponse userInfoResponse = userClient.getUserById(saved.getUserId());

        OrderResponse response = orderMapper.toResponse(saved);
        return enrichWithUserInfo(response, userInfoResponse);
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
                    OrderResponse response = orderMapper.toResponse(order);
                    return enrichWithUserInfo(response, userInfo);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(orderResponses, pageable, ordersPage.getTotalElements());
    }

}
