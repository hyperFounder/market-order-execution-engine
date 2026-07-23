package com.hft.engine.marketorderexecutionengine.controller;

import com.hft.engine.marketorderexecutionengine.dto.OrderRequestDto;
import com.hft.engine.marketorderexecutionengine.dto.OrderResponseDto;
import com.hft.engine.marketorderexecutionengine.entity.OrderEntity;
import com.hft.engine.marketorderexecutionengine.service.OrderRouterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1/orders")
public class OrderIngestionController {

    private final OrderRouterService orderRouterService;

    public OrderIngestionController(OrderRouterService orderRouterService){
        this.orderRouterService = orderRouterService;
    }

    @PostMapping("/submit")
    public ResponseEntity<OrderResponseDto> submitOrder(@Valid @RequestBody OrderRequestDto requestDto){
        OrderResponseDto response = orderRouterService.processOrder(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderEntity> getOrder(@PathVariable String orderId){
        OrderEntity order = orderRouterService.getOrderById(orderId);
        if (order == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }
}
