package com.portly.exception;

public class SamePasswordException extends RuntimeException {
    
    public SamePasswordException() {
        super("La nueva contraseña no puede ser igual a la actual.");
    }
    
    public SamePasswordException(String message) {
        super(message);
    }
}