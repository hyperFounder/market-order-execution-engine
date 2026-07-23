package com.hft.engine.marketorderexecutionengine.service;

import com.hft.engine.marketorderexecutionengine.dto.OrderRequestDto;
import com.hft.engine.marketorderexecutionengine.dto.OrderResponseDto;
import com.hft.engine.marketorderexecutionengine.entity.OrderEntity;
import com.hft.engine.marketorderexecutionengine.exception.InsufficientLiquidityException;
import com.hft.engine.marketorderexecutionengine.mapper.OrderMapper;
import com.hft.engine.marketorderexecutionengine.repository.InMemoryOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderRouterService {

    private static final Logger log = LoggerFactory.getLogger(OrderRouterService.class);
    private final OrderMapper orderMapper;
    private final InMemoryOrderRepository orderRepository;

    public OrderRouterService(OrderMapper orderMapper, InMemoryOrderRepository orderRepository){
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
    }

    public OrderResponseDto processOrder(OrderRequestDto requestDto){

        if (requestDto.quantity() > 1_000_000) {
            throw new InsufficientLiquidityException(requestDto.symbol(), requestDto.quantity());
        }

        // Map DTO -> Entity
        OrderEntity orderEntity = orderMapper.toEntity(requestDto);

        // Save to repository
        orderRepository.save(orderEntity);

        // Async handoff
        routeToExchange(orderEntity);

        // Return acknowledgement
        return orderMapper.toResponseDto(orderEntity);
    }

    public CompletableFuture<Void> routeToExchange(OrderEntity order){
        long startTime = System.nanoTime();
        log.info("[{}] Routing ORDER_ID: {} | {} {} units of {}",
                Thread.currentThread().getName(), order.getOrderId(), order.getSide(), order.getQuantity(), order.getSymbol());

        try{
            Thread.sleep(5); // Simulate market network delay
            order.setStatus("FILLED");

            // UpdateStatus in memory store automatically
            orderRepository.save(order);

            long duractionMicros = (System.nanoTime() - startTime) / 1000;
            log.info("[{}] ORDER_ID: {} FILLED IN {} µs ",
                    Thread.currentThread().getName(), order.getOrderId(), duractionMicros);

        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            order.setStatus("REJECTED");
        }
        return CompletableFuture.completedFuture(null);
    }

    public OrderEntity getOrderById(String orderId){
        return orderRepository.findById(orderId).orElse(null);
    }
}

