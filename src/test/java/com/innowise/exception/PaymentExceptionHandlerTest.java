package com.innowise.exception;

import com.innowise.model.dto.ErrorDto;
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
    void handlePaymentNotFoundException_ShouldReturnNotFoundStatus() {
        // given
        PaymentNotFoundException exception = new PaymentNotFoundException();

        // when
        ResponseEntity<ErrorDto> response = handler.handlePaymentServiceException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorMessage.PAYMENT_NOT_FOUND.name());
        assertThat(response.getBody().getErrorMessage()).isEqualTo(ErrorMessage.PAYMENT_NOT_FOUND.getMessage());
    }

    @Test
    void handleGenericPaymentServiceException_ShouldReturnInternalServerError() {
        // given
        PaymentServiceException exception = new PaymentServiceException(ErrorMessage.PAYMENT_NOT_FOUND);

        // when
        ResponseEntity<ErrorDto> response = handler.handlePaymentServiceException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorMessage.PAYMENT_NOT_FOUND.name());
        assertThat(response.getBody().getErrorMessage()).isEqualTo(ErrorMessage.PAYMENT_NOT_FOUND.getMessage());
    }

    @Test
    void determineHttpStatus_ShouldReturnNotFound_ForPaymentNotFoundException() throws Exception {
        var method = PaymentExceptionHandler.class
                .getDeclaredMethod("determineHttpStatus", PaymentServiceException.class);
        method.setAccessible(true);

        HttpStatus status = (HttpStatus) method.invoke(handler, new PaymentNotFoundException());

        assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void determineHttpStatus_ShouldReturnInternalServerError_ForGenericException() throws Exception {
        var method = PaymentExceptionHandler.class
                .getDeclaredMethod("determineHttpStatus", PaymentServiceException.class);
        method.setAccessible(true);

        HttpStatus status = (HttpStatus) method.invoke(handler,
                new PaymentServiceException(ErrorMessage.PAYMENT_NOT_FOUND));

        assertThat(status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
