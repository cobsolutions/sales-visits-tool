package com.sales.visits.app.sales.exception;

public class PasswordResetNotAllowedException extends RuntimeException {
    public PasswordResetNotAllowedException(String message) {
        super(message);
    }
}
