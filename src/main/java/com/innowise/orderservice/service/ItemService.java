package com.innowise.orderservice.service;

import com.innowise.orderservice.entity.ItemEntity;
import com.innowise.orderservice.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ItemEntity findById(Long id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Item with id " + id + " not found")
        );
    }

    public boolean existsById(Long id) {
        return itemRepository.existsById(id);
    }

    public BigDecimal getItemPrice(Long id) {
        return findById(id).getPrice();
    }
}
