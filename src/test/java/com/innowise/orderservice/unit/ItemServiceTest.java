package com.innowise.orderservice.unit;

import com.innowise.orderservice.dto.ItemResponse;
import com.innowise.orderservice.entity.ItemEntity;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    private ItemEntity testItem;
    private ItemResponse testItemResponse;

    @BeforeEach
    void setUp() {
        testItem = new ItemEntity();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(BigDecimal.valueOf(99.99));

        testItemResponse = new ItemResponse(1L, "Test Item", BigDecimal.valueOf(99.99));
    }

    @Test
    void findById_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        ItemEntity result = itemService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Item");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(99.99));
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NotFound_ThrowsException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Item with id 999 not found");

        verify(itemRepository, times(1)).findById(999L);
    }

    @Test
    void findAll_Success() {
        List<ItemEntity> items = List.of(testItem);
        when(itemRepository.findAll()).thenReturn(items);
        when(itemMapper.toDto(testItem)).thenReturn(testItemResponse);

        List<ItemResponse> result = itemService.findAll();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Test Item");
        verify(itemRepository, times(1)).findAll();
        verify(itemMapper, times(1)).toDto(testItem);
    }

    @Test
    void findAll_EmptyList_ReturnsEmptyList() {
        when(itemRepository.findAll()).thenReturn(List.of());

        List<ItemResponse> result = itemService.findAll();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(itemRepository, times(1)).findAll();
        verify(itemMapper, never()).toDto(any());
    }
}