package com.innowise.exception;

import com.innowise.model.dto.ErrorDto;
import com.innowise.model.enums.ErrorMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
                .isEqualTo(ErrorMessage.PAYMENT_NOT_FOUND.getCode());
        assertThat(response.getBody().getErrorMessage())
                .isEqualTo(ErrorMessage.PAYMENT_NOT_FOUND.getDefaultMessage());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        Exception exception = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorDto> response = handler.handleGenericException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode())
                .isEqualTo(ErrorMessage.INTERNAL_SERVER_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage())
                .isEqualTo(ErrorMessage.INTERNAL_SERVER_ERROR.getDefaultMessage());
    }

    @Test
    void handleMethodArgumentNotValidException_ShouldReturnBadRequest() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("payment", "amount", "Amount must be positive");
        FieldError fieldError2 = new FieldError("payment", "orderId", "Order ID is required");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ErrorDto> response = handler.handleMethodArgumentNotValidException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode())
                .isEqualTo(ErrorMessage.VALIDATION_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage())
                .contains("Amount must be positive")
                .contains("Order ID is required");
    }

    @Test
    void handleMethodArgumentNotValidException_ShouldReturnSingleError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("payment", "userId", "User ID cannot be null");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorDto> response = handler.handleMethodArgumentNotValidException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorMessage())
                .isEqualTo("User ID cannot be null");
    }

    @Test
    void handleConstraintViolationException_ShouldReturnBadRequest() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);

        when(violation1.getMessage()).thenReturn("Payment amount must be greater than zero");
        when(violation2.getMessage()).thenReturn("Payment ID must not be blank");

        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<ErrorDto> response = handler.handleConstraintViolationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode())
                .isEqualTo(ErrorMessage.VALIDATION_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage())
                .containsAnyOf("Payment amount must be greater than zero", "Payment ID must not be blank");
    }

    @Test
    void handleConstraintViolationException_ShouldReturnSingleViolation() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid payment status");

        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<ErrorDto> response = handler.handleConstraintViolationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorMessage())
                .contains("Invalid payment status");
    }

    @Test
    void handleGenericException_ShouldHandleDifferentExceptionTypes() {
        Exception nullPointerException = new NullPointerException("Null value encountered");

        ResponseEntity<ErrorDto> response = handler.handleGenericException(nullPointerException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode())
                .isEqualTo(ErrorMessage.INTERNAL_SERVER_ERROR.getCode());
    }

    @Test
    void handleGenericException_ShouldHandleIllegalArgumentException() {
        Exception illegalArgumentException = new IllegalArgumentException("Invalid argument provided");

        ResponseEntity<ErrorDto> response = handler.handleGenericException(illegalArgumentException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
    }
}
