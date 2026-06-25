package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.ItemResponse;
import com.innowise.orderservice.entity.ItemEntity;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final Logger log = LoggerFactory.getLogger(ItemController.class);
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    public ItemController(ItemService itemService, ItemMapper itemMapper) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> findById(@PathVariable Long id) {
        log.info("In ItemController findById");
        ItemEntity item = itemService.findById(id);
        return ResponseEntity.ok(itemMapper.toDto(item));
    }

    @GetMapping
    public ResponseEntity<List<ItemResponse>> findAll() {
        log.info("In ItemController findAll");
        return ResponseEntity.ok(itemService.findAll());
    }
}
