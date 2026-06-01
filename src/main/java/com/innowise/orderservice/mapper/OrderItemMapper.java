package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderItemRequest;
import com.innowise.orderservice.dto.OrderItemResponse;
import com.innowise.orderservice.entity.OrderItemEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemResponse toDto(OrderItemEntity item);
    OrderItemEntity toEntity(OrderItemRequest dto);
}
