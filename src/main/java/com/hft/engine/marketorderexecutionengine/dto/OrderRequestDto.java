package com.hft.engine.marketorderexecutionengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderRequestDto(

        @NotBlank(message = "Symbol is required")
        String symbol,

        @NotBlank(message = "Side must be BUY or SELL")
        String side,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than zero")
        BigDecimal price,

        @Positive(message = "Quantity must be greater than zero")
        long quantity
){}
