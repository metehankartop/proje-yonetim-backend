package com.metehankartop.proje_yonetim.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        System.err.println("=== GLOBAL EXCEPTION HANDLER ===");
        System.err.println("Exception type: " + ex.getClass().getSimpleName());
        System.err.println("Exception message: " + ex.getMessage());
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Sunucu hatası: " + ex.getMessage(),
                        "type", ex.getClass().getSimpleName(),
                        "timestamp", System.currentTimeMillis()
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        System.err.println("=== RUNTIME EXCEPTION ===");
        System.err.println("Exception message: " + ex.getMessage());
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", ex.getMessage(),
                        "type", "RuntimeException",
                        "timestamp", System.currentTimeMillis()
                ));
    }
}