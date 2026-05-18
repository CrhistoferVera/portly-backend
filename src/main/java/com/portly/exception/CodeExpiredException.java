package com.portly.exception;

public class CodeExpiredException extends RuntimeException {
    public CodeExpiredException() {
        super("El código de verificación ha expirado. Por favor, solicita uno nuevo.");
    }
}