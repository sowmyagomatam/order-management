package com.ecommerce.order.exception;

import com.ecommerce.order.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException e,
                                                                      HttpServletRequest req){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .error("Not found")
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderStateException(OrderNotFoundException e,
                                                                      HttpServletRequest req){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .error("Invalid order state")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e,
                                                                HttpServletRequest req){

       String error =  e.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ":" + fieldError.getDefaultMessage())
                .collect(Collectors.joining(","));
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .message(error)
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation failed")
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> genericHandler(Exception e,
                                                        HttpServletRequest req){

        return ResponseEntity.internalServerError().body(
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .error("Internal server error")
                        .message("An unexpected error occured")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .path(req.getRequestURI())
                        .build()
        );
    }
}
