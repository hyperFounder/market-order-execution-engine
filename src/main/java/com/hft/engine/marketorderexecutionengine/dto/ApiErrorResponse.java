package com.hft.engine.marketorderexecutionengine.dto;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
    String errorCode,
    String message,
    List<String> details,
    Instant timestamp
){}
