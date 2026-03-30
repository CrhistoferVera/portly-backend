package com.portly.exception;

public class InvalidCodeException extends RuntimeException {
    public InvalidCodeException() {
        super("El código de verificación es incorrecto.");
    }
}