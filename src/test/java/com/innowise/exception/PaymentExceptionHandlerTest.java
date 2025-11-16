package com.innowise.exception;

import com.innowise.model.dto.ErrorDto;
import com.innowise.model.enums.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentExceptionHandlerTest {

    private PaymentExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PaymentExceptionHandler();
    }

    @Test
    void handlePaymentNotFoundException_ShouldReturnNotFound() {
        PaymentNotFoundException exception = new PaymentNotFoundException();

        ResponseEntity<ErrorDto> response = handler.handlePaymentNotFoundException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode())
                .isEqualTo(ErrorMessage.PAYMENT_NOT_FOUND.name());
        assertThat(response.getBody().getErrorMessage())
                .isEqualTo(ErrorMessage.PAYMENT_NOT_FOUND.getMessage());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        Exception exception = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorDto> response = handler.handleGenericException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode())
                .isEqualTo(ErrorMessage.INTERNAL_SERVER_ERROR.name());
        assertThat(response.getBody().getErrorMessage())
                .isEqualTo(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage());
    }
}
