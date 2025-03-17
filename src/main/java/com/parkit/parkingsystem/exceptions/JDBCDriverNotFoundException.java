package com.parkit.parkingsystem.exceptions;

public class JDBCDriverNotFoundException extends RuntimeException {
    public JDBCDriverNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
