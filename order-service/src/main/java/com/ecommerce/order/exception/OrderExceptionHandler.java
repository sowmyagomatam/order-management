package com.ecommerce.order.exception;

import com.ecommerce.common.dto.ErrorResponse;
import com.ecommerce.common.exception.BaseExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class OrderExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException e,
                                                                      HttpServletRequest req){
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.NOT_FOUND,
                "Not found",
                e.getMessage(),
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderStateException(OrderNotFoundException e,
                                                                          HttpServletRequest req){

        ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST,
                "Invalid order state",
                e.getMessage(),
                req.getRequestURI());

        return ResponseEntity.badRequest().body(errorResponse);
    }

}
