package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderItemResponse;
import com.innowise.orderservice.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemPrice", source = "item.price")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "subtotal", ignore = true)
    OrderItemResponse toDto(OrderItemEntity entity);


    List<OrderItemResponse> toDtoList(List<OrderItemEntity> entities);

}
