package com.spendsense.exception;

public class InvalidRecurringTransactionException extends RuntimeException {
    public InvalidRecurringTransactionException(String message) {
        super(message);
    }
}
