package com.hft.engine.marketorderexecutionengine.mapper;

import com.hft.engine.marketorderexecutionengine.dto.OrderRequestDto;
import com.hft.engine.marketorderexecutionengine.dto.OrderResponseDto;
import com.hft.engine.marketorderexecutionengine.entity.OrderEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Test
    @DisplayName("Should correctly map OrderRequestDto to OrderEntity with defaults")
    void toEntity_ShouldMapFieldsAndGenerateDefaults() {
        // Given
        OrderRequestDto request = new OrderRequestDto("NVDA", "BUY", new BigDecimal("130.50"), 500);

        // When
        OrderEntity entity = mapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getSymbol()).isEqualTo("NVDA");
        assertThat(entity.getSide()).isEqualTo("BUY");
        assertThat(entity.getPrice()).isEqualTo(new BigDecimal("130.50"));
        assertThat(entity.getQuantity()).isEqualTo(500);
        assertThat(entity.getOrderId()).isNotNull();
        assertThat(entity.getStatus()).isEqualTo("PENDING_ROUTE");
        assertThat(entity.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should correctly map OrderEntity to OrderResponseDto")
    void toResponseDto_ShouldMapEntityToResponse() {
        // Given
        OrderEntity entity = new OrderEntity();
        entity.setOrderId("ORD-999");
        entity.setSymbol("AAPL");
        entity.setStatus("PENDING_ROUTE");

        // When
        OrderResponseDto response = mapper.toResponseDto(entity);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo("ORD-999");
        assertThat(response.symbol()).isEqualTo("AAPL");
        assertThat(response.status()).isEqualTo("PENDING_ROUTE");
        assertThat(response.executionRoute()).isEqualTo("DIRECT_EXCHANGE_DMA");
    }
}