package com.bobocode.exception;

public class CompanyDaoException extends RuntimeException{
    public CompanyDaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
