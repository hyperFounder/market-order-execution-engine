package com.hft.engine.marketorderexecutionengine.exception;

import com.hft.engine.marketorderexecutionengine.dto.ApiErrorResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource){
        this.messageSource = messageSource;
    }

    @ExceptionHandler(InsufficientLiquidityException.class)
    public ResponseEntity<ApiErrorResponse> handleLiquidityException(InsufficientLiquidityException ex){
        String localizedMessage = messageSource.getMessage(
                "error.msg.liquidity",
                new Object[]{ex.getSymbol(), ex.getQuantity()},
                LocaleContextHolder.getLocale()
        );

        ApiErrorResponse error = new ApiErrorResponse(
                "INSUFFICIENT_LIQUIDITY",
                localizedMessage,
                List.of(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        ApiErrorResponse error = new ApiErrorResponse(
                "VALIDATION_FAILED",
                "Invalid request payload",
                fieldErrors,
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
}
