package com.ecommerce.payment.exception;

import com.ecommerce.common.dto.ErrorResponse;
import com.ecommerce.common.exception.BaseExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PaymentExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    ResponseEntity<ErrorResponse> handlePaymentNotFoundException(PaymentNotFoundException e,
                                                                 HttpServletRequest req){
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.NOT_FOUND,
                "Not found",
                e.getMessage(),
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    ResponseEntity<ErrorResponse> handleInvalidPaymentStateException(InvalidPaymentStateException e,
                                                                 HttpServletRequest req){
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST,
                "Invalid payment state",
                e.getMessage(),
                req.getRequestURI());
        return ResponseEntity.badRequest().body(errorResponse);

    }
}
