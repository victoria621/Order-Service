package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.ItemResponse;
import com.innowise.orderservice.entity.ItemEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemResponse toDto(ItemEntity item);
}
