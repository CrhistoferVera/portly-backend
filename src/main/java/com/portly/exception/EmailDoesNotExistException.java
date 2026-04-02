package com.portly.exception;

public class EmailDoesNotExistException extends RuntimeException{
    
    public EmailDoesNotExistException(String email){
        super("El correo electrónico no fue encontrado: " + email);
    }
}
