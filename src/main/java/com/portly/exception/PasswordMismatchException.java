package com.portly.exception;

public class PasswordMismatchException extends RuntimeException {

    public PasswordMismatchException() {
        super("La contraseña y su confirmación no coinciden");
    }
}
