package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.ItemResponse;
import com.innowise.orderservice.entity.ItemEntity;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemService(ItemRepository itemRepository, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    public ItemEntity findById(Long id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Item with id " + id + " not found")
        );
    }

    public List<ItemResponse> findAll() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toDto)
                .toList();
    }
}
