package com.innowise.exception;

import com.innowise.model.enums.ErrorMessage;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException() {
        super(ErrorMessage.PAYMENT_NOT_FOUND.getDefaultMessage());
    }

    public PaymentNotFoundException(Throwable cause) {
        super(ErrorMessage.PAYMENT_NOT_FOUND.getDefaultMessage(), cause);
    }
}
