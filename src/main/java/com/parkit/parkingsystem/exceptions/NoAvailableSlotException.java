package com.parkit.parkingsystem.exceptions;

public class NoAvailableSlotException extends RuntimeException {
    public NoAvailableSlotException(String message) {
        super(message);
    }
}
