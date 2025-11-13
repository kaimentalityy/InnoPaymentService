package com.innowise.exception;

import com.innowise.model.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for Payment Service.
 */
@RestControllerAdvice
public class PaymentExceptionHandler {

    /**
     * Handle all PaymentServiceException exceptions.
     */
    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<ErrorDto> handlePaymentServiceException(PaymentServiceException ex) {
        ErrorDto errorResponse = new ErrorDto(
                ex.getErrorMessage().name(),
                ex.getErrorMessage().getMessage()
        );

        HttpStatus status = determineHttpStatus(ex);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Map exception types to HTTP status codes.
     */
    private HttpStatus determineHttpStatus(PaymentServiceException ex) {
        if (ex instanceof PaymentNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
