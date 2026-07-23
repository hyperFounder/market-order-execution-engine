package com.hft.engine.marketorderexecutionengine.dto;

import java.time.Instant;

public record OrderResponseDto(
        String orderId,
        String symbol,
        String status,
        Instant timestamp,
        String executionRoute
){}
