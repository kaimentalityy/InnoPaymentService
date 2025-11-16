package com.innowise.exception;

import com.innowise.model.dto.ErrorDto;
import com.innowise.model.enums.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class PaymentExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorDto> handlePaymentNotFoundException(PaymentNotFoundException ex) {
        log.warn("Payment not found: {}", ex.getMessage());

        ErrorDto error = new ErrorDto(
                ErrorMessage.PAYMENT_NOT_FOUND.name(),
                ErrorMessage.PAYMENT_NOT_FOUND.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(Exception ex) {
        log.error("Unhandled error", ex);

        ErrorDto error = new ErrorDto(
                ErrorMessage.INTERNAL_SERVER_ERROR.name(),
                ErrorMessage.INTERNAL_SERVER_ERROR.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
