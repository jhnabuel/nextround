package com.nextround.nextroundapi.exception;

public class InvalidCurrentPasswordException extends RuntimeException {
    public InvalidCurrentPasswordException(String message) {
        super(message);
    }
}
