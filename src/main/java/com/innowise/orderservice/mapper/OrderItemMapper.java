package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderItemResponse;
import com.innowise.orderservice.entity.OrderItemEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    @Mapping(target = "itemPrice", source = "item.price")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "subtotal", ignore = true)
    OrderItemResponse toDto(OrderItemEntity item);


    List<OrderItemResponse> toDtoList(List<OrderItemEntity> entities);

    @AfterMapping
    default BigDecimal calculateSubtotal(OrderItemEntity entity) {
        if (entity == null || entity.getItem() == null || entity.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return entity.getItem().getPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
    }
}
