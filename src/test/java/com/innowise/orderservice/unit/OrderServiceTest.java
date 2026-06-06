package com.innowise.orderservice.unit;

import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.entity.ItemEntity;
import com.innowise.orderservice.entity.OrderEntity;
import com.innowise.orderservice.entity.OrderStatus;
import com.innowise.orderservice.mapper.OrderItemMapper;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.ItemService;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.service.UserClient;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ItemService itemService;

    @Mock
    private UserClient userClient;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderService orderService;

    private OrderEntity testOrder;
    private UserInfoResponse testUserInfo;
    private CreateOrderRequest createOrderRequest;
    private UpdateOrderRequest updateOrderRequest;

    @BeforeEach
    void setUp() {
        testUserInfo = new UserInfoResponse(1L, "test@email.com", "Test", "User", true);

        testOrder = new OrderEntity();
        testOrder.setId(1L);
        testOrder.setUserId(1L);
        testOrder.setStatus(OrderStatus.NEW);
        testOrder.setTotalPrice(BigDecimal.valueOf(100));
        testOrder.setDeleted(false);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
        testOrder.setOrderItems(new ArrayList<>());

        createOrderRequest = new CreateOrderRequest(1L, List.of(
                new OrderItemRequest(1L, 2)
        ));

        updateOrderRequest = new UpdateOrderRequest(OrderStatus.PROCESSING, null);
    }

    @Test
    void createOrder_Success() {
        ItemEntity item = new ItemEntity();
        item.setId(1L);
        item.setPrice(BigDecimal.valueOf(50));

        when(itemService.findById(1L)).thenReturn(item);
        when(orderMapper.toEntity(createOrderRequest)).thenReturn(testOrder);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(testOrder);
        when(userClient.getUserById(1L)).thenReturn(testUserInfo);

        OrderResponse response = orderService.createOrder(createOrderRequest);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(1L);
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userClient.getUserById(1L)).thenReturn(testUserInfo);

        OrderResponse response = orderService.getOrderById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order with id: 999 not found");
    }

    @Test
    void updateOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(testOrder);
        when(userClient.getUserById(1L)).thenReturn(testUserInfo);

        OrderResponse response = orderService.updateOrder(1L, updateOrderRequest);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(OrderStatus.PROCESSING);
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void deleteOrder_SoftDelete_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(testOrder);

        orderService.deleteOrder(1L);

        assertThat(testOrder.isDeleted()).isTrue();
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void getOrdersByUserId_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> ordersPage = new PageImpl<>(List.of(testOrder), pageable, 1);

        when(orderRepository.findByUserId(1L, pageable)).thenReturn(ordersPage);
        when(userClient.getUserById(1L)).thenReturn(testUserInfo);

        Page<OrderResponse> response = orderService.getOrdersByUserId(1L, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void getOrdersWithFilters_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> ordersPage = new PageImpl<>(List.of(testOrder), pageable, 1);

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(ordersPage);
        when(userClient.getUserById(1L)).thenReturn(testUserInfo);

        Page<OrderResponse> response = orderService.getOrdersWithFilters(
                pageable, null, null, List.of(OrderStatus.NEW)
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(orderRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }
}