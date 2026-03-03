package com.example.smarteducationsystem_back.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Parameter error: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("System error: ", e);
        // Note: For simple 403 authorization we'll handle directly in interceptor or throw custom exception.
        if (e.getMessage() != null && e.getMessage().contains("Access Denied")) {
            return Result.error(403, "无权限访问");
        }
        return Result.error(500, "服务器开小差了：" + e.getMessage());
    }
}
