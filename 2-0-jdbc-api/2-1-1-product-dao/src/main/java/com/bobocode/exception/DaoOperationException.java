package com.bobocode.exception;

public class DaoOperationException extends RuntimeException {
    public DaoOperationException(String message) {
        super(message);
    }

    public DaoOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
