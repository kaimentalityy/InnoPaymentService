package com.innowise.exception;

import com.innowise.model.dto.ErrorDto;
import com.innowise.model.enums.ErrorMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for the Payment Service REST API.
 * <p>
 * This class handles exceptions thrown by controllers and services,
 * converting them into appropriate HTTP responses with standardized
 * error messages.
 * </p>
 * <p>
 * Handles the following exception types:
 * <ul>
 * <li>{@link PaymentNotFoundException} - 404 Not Found</li>
 * <li>{@link MethodArgumentNotValidException} - 400 Bad Request (validation
 * errors)</li>
 * <li>{@link ConstraintViolationException} - 400 Bad Request (constraint
 * violations)</li>
 * <li>{@link Exception} - 500 Internal Server Error (generic errors)</li>
 * </ul>
 * </p>
 *
 * @see ErrorDto
 * @see ErrorMessage
 */
@Slf4j
@RestControllerAdvice
public class PaymentExceptionHandler {

        /**
         * Handles {@link PaymentNotFoundException} and returns a 404 Not Found
         * response.
         *
         * @param ex the payment not found exception
         * @return ResponseEntity with error details and 404 status
         */
        @ExceptionHandler(PaymentNotFoundException.class)
        public ResponseEntity<ErrorDto> handlePaymentNotFoundException(PaymentNotFoundException ex) {
                log.warn("Payment not found: {}", ex.getMessage());

                ErrorDto error = new ErrorDto(
                                ErrorMessage.PAYMENT_NOT_FOUND.getCode(),
                                ErrorMessage.PAYMENT_NOT_FOUND.getDefaultMessage());
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        /**
         * Handles validation errors on {@code @RequestBody} DTOs.
         * <p>
         * This method is triggered when {@code @Valid} annotation validation fails.
         * It collects all field error messages and returns them in a single error
         * response.
         * </p>
         *
         * @param ex the method argument not valid exception
         * @return ResponseEntity with validation error details and 400 status
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
                String errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.joining(", "));
                log.warn("Validation failed: {}", errors);

                ErrorDto error = new ErrorDto(
                                ErrorMessage.VALIDATION_ERROR.getCode(),
                                errors);
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles constraint violation exceptions.
         * <p>
         * This method is triggered when bean validation constraints are violated.
         * It collects all constraint violation messages and returns them in a single
         * error response.
         * </p>
         *
         * @param ex the constraint violation exception
         * @return ResponseEntity with constraint violation details and 400 status
         */
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorDto> handleConstraintViolationException(ConstraintViolationException ex) {
                String errors = ex.getConstraintViolations()
                                .stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));
                log.warn("Constraint violation: {}", errors);

                ErrorDto error = new ErrorDto(
                                ErrorMessage.VALIDATION_ERROR.getCode(),
                                errors);
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles all unhandled exceptions.
         * <p>
         * This is a catch-all handler for any exceptions not explicitly handled
         * by other exception handlers. It logs the full stack trace and returns
         * a generic internal server error response.
         * </p>
         *
         * @param ex the unhandled exception
         * @return ResponseEntity with generic error message and 500 status
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorDto> handleGenericException(Exception ex) {
                log.error("Unhandled error", ex);

                ErrorDto error = new ErrorDto(
                                ErrorMessage.INTERNAL_SERVER_ERROR.getCode(),
                                ErrorMessage.INTERNAL_SERVER_ERROR.getDefaultMessage());
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
