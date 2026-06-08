package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.CreateOrderRequest;
import com.innowise.orderservice.entity.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",uses = {OrderItemMapper.class})
public interface OrderMapper {
    OrderEntity toEntity(CreateOrderRequest request);
}
