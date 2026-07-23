package com.hft.engine.marketorderexecutionengine.repository;

import com.hft.engine.marketorderexecutionengine.entity.OrderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryOrderRepositoryTest {

    private InMemoryOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
    }

    @Test
    @DisplayName("Should save and retrieve an order by ID")
    void saveAndFindById_Success() {
        // Given
        OrderEntity order = new OrderEntity();
        order.setOrderId("ORD-101");
        order.setSymbol("NVDA");
        order.setSide("BUY");
        order.setPrice(new BigDecimal("120.00"));
        order.setQuantity(100);
        order.setStatus("PENDING_ROUTE");
        order.setTimestamp(Instant.now());

        // When
        repository.save(order);
        Optional<OrderEntity> retrieved = repository.findById("ORD-101");

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getOrderId()).isEqualTo("ORD-101");
        assertThat(retrieved.get().getSymbol()).isEqualTo("NVDA");
    }

    @Test
    @DisplayName("Should return empty Optional when order ID does not exist")
    void findById_NotFound_ReturnsEmptyOptional() {
        // When
        Optional<OrderEntity> retrieved = repository.findById("NON_EXISTENT_ID");

        // Then
        assertThat(retrieved).isEmpty();
    }

    @Test
    @DisplayName("Should return all saved orders using findAll")
    void findAll_ReturnsAllStoredOrders() {
        // Given
        OrderEntity order1 = new OrderEntity();
        order1.setOrderId("ORD-1");

        OrderEntity order2 = new OrderEntity();
        order2.setOrderId("ORD-2");

        repository.save(order1);
        repository.save(order2);

        // When
        Map<String, OrderEntity> allOrders = repository.findAll();

        // Then
        assertThat(allOrders).hasSize(2);
        assertThat(allOrders).containsKeys("ORD-1", "ORD-2");
    }
}
