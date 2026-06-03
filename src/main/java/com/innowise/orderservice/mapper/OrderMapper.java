package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.CreateOrderRequest;
import com.innowise.orderservice.dto.OrderResponse;
import com.innowise.orderservice.dto.UpdateOrderRequest;
import com.innowise.orderservice.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",uses = {OrderItemMapper.class})
public interface OrderMapper {
    OrderEntity toEntity(CreateOrderRequest request);
    void updateEntity(@MappingTarget OrderEntity entity, UpdateOrderRequest request);
    OrderResponse toDto(OrderEntity order);
}
