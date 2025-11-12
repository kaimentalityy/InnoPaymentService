package com.innowise.exception;

import java.io.Serial;

public class PaymentNotFoundException extends PaymentServiceException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PaymentNotFoundException() {
        super(ErrorMessage.PAYMENT_NOT_FOUND);
    }

    public PaymentNotFoundException(Throwable cause) {
        super(ErrorMessage.PAYMENT_NOT_FOUND, cause);
    }
}
