package com.hft.engine.marketorderexecutionengine.service;

import com.hft.engine.marketorderexecutionengine.dto.OrderRequestDto;
import com.hft.engine.marketorderexecutionengine.dto.OrderResponseDto;
import com.hft.engine.marketorderexecutionengine.entity.OrderEntity;
import com.hft.engine.marketorderexecutionengine.exception.InsufficientLiquidityException;
import com.hft.engine.marketorderexecutionengine.mapper.OrderMapper;
import com.hft.engine.marketorderexecutionengine.repository.InMemoryOrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRouterServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private InMemoryOrderRepository orderRepository;

    @InjectMocks
    private OrderRouterService orderRouterService;

    @Test
    @DisplayName("Should process order, save to repository, and return response DTO")
    void processOrder_Success() {
        // Given
        OrderRequestDto request = new OrderRequestDto("NVDA", "BUY", new BigDecimal("130.00"), 100);
        OrderEntity entity = new OrderEntity();
        entity.setOrderId("ORD-123");

        OrderResponseDto responseDto = new OrderResponseDto("ORD-123", "NVDA", "PENDING_ROUTE", Instant.now(), "DMA");

        when(orderMapper.toEntity(any())).thenReturn(entity);
        when(orderMapper.toResponseDto(any())).thenReturn(responseDto);

        // When
        OrderResponseDto result = orderRouterService.processOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo("ORD-123");

        verify(orderRepository, times(2)).save(entity);
    }

    @Test
    @DisplayName("Should throw InsufficientLiquidityException when quantity > 1,000,000")
    void processOrder_ExceedsLiquidity_ThrowsException() {
        // Given
        OrderRequestDto request = new OrderRequestDto("TSLA", "BUY", new BigDecimal("200.00"), 2_000_000);

        // When & Then
        assertThatThrownBy(() -> orderRouterService.processOrder(request))
                .isInstanceOf(InsufficientLiquidityException.class);

        verifyNoInteractions(orderRepository);
        verifyNoInteractions(orderMapper);
    }

    @Test
    @DisplayName("Should fetch order by ID from repository")
    void getOrderById_CallsRepository() {
        // Given
        OrderEntity entity = new OrderEntity();
        entity.setOrderId("ORD-123");

        when(orderRepository.findById("ORD-123")).thenReturn(Optional.of(entity));

        // When
        OrderEntity result = orderRouterService.getOrderById("ORD-123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo("ORD-123");
        verify(orderRepository, times(1)).findById("ORD-123");
    }
}