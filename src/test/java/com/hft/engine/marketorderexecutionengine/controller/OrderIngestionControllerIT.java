package com.hft.engine.marketorderexecutionengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import com.hft.engine.marketorderexecutionengine.config.HftAsyncConfig;
import com.hft.engine.marketorderexecutionengine.dto.OrderRequestDto;
import com.hft.engine.marketorderexecutionengine.dto.OrderResponseDto;
import com.hft.engine.marketorderexecutionengine.entity.OrderEntity;
import com.hft.engine.marketorderexecutionengine.exception.InsufficientLiquidityException;
import com.hft.engine.marketorderexecutionengine.service.OrderRouterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderIngestionController.class)
@Import(HftAsyncConfig.class)
class OrderIngestionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderRouterService orderRouterService;

    @Test
    @DisplayName("POST /api/v1/orders/submit - Valid order returns 202 ACCEPTED")
    void submitOrder_ValidPayload_Returns202Accepted() throws Exception {

        OrderRequestDto request =
                new OrderRequestDto("NVDA", "BUY", new BigDecimal("125.00"), 500);

        OrderResponseDto response =
                new OrderResponseDto(
                        "ORD-5555",
                        "NVDA",
                        "PENDING_ROUTE",
                        Instant.now(),
                        "DIRECT_EXCHANGE_DMA"
                );

        when(orderRouterService.processOrder(any(OrderRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value("ORD-5555"))
                .andExpect(jsonPath("$.symbol").value("NVDA"))
                .andExpect(jsonPath("$.status").value("PENDING_ROUTE"))
                .andExpect(jsonPath("$.executionRoute").value("DIRECT_EXCHANGE_DMA"));
    }

    @Test
    @DisplayName("POST /api/v1/orders/submit - Invalid payload triggers Bean Validation")
    void submitOrder_InvalidPayload_Returns422() throws Exception {

        OrderRequestDto invalidRequest =
                new OrderRequestDto("", "BUY", new BigDecimal("0.00"), -10);

        mockMvc.perform(post("/api/v1/orders/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/orders/submit - Insufficient liquidity returns 400 BAD_REQUEST")
    void submitOrder_InsufficientLiquidity_Returns400() throws Exception {

        OrderRequestDto request =
                new OrderRequestDto("TSLA", "BUY", new BigDecimal("250.00"), 5_000_000);

        when(orderRouterService.processOrder(any(OrderRequestDto.class)))
                .thenThrow(new InsufficientLiquidityException("TSLA", 5_000_000));

        mockMvc.perform(post("/api/v1/orders/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_LIQUIDITY"))
                .andExpect(jsonPath("$.message")
                        .value("Execution rejected: Insufficient market liquidity for symbol TSLA at requested order size 5000000."));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - Returns 200 OK when order exists")
    void getOrder_Exists_Returns200OK() throws Exception {
        // Given
        String orderId = "ORD-777";
        OrderEntity mockEntity = new OrderEntity();
        mockEntity.setOrderId(orderId);
        mockEntity.setSymbol("AAPL");
        mockEntity.setStatus("FILLED");

        when(orderRouterService.getOrderById(orderId)).thenReturn(mockEntity);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.status").value("FILLED"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - Returns 404 NOT_FOUND when order does not exist")
    void getOrder_DoesNotExist_Returns404NotFound() throws Exception {
        // Given
        String orderId = "UNKNOWN_ID";
        when(orderRouterService.getOrderById(orderId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isNotFound());
    }
}