package com.hft.engine.marketorderexecutionengine.exception;

import com.hft.engine.marketorderexecutionengine.dto.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler(messageSource);
    }

    @Test
    @DisplayName("Should handle InsufficientLiquidityException and format localized response")
    void handleLiquidityException_ReturnsBadRequestWithLocalizedMsg() {
        // Given
        InsufficientLiquidityException ex = new InsufficientLiquidityException("TSLA", 2_000_000);
        String expectedMessage = "Execution rejected: Insufficient market liquidity for symbol TSLA at requested order size 2000000.";

        when(messageSource.getMessage(
                eq("error.msg.liquidity"),
                any(Object[].class),
                any(Locale.class)
        )).thenReturn(expectedMessage);

        // When
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleLiquidityException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("INSUFFICIENT_LIQUIDITY");
        assertThat(response.getBody().message()).isEqualTo(expectedMessage);
    }
}