package com.crs.iamservice.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        int status,
        String message,
        String path,
        LocalDateTime timestamp
) {}
