package com.portly.exception;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return warnResponse(HttpStatus.CONFLICT, "Registro rechazado - email duplicado: {}", ex);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<Map<String, String>> handlePasswordMismatch(PasswordMismatchException ex) {
        return warnResponse(HttpStatus.BAD_REQUEST, "Credenciales inválidas: {}", ex);
    }

    @ExceptionHandler(EmailDoesNotExistException.class)
    public ResponseEntity<Map<String, String>> handleEmailError(EmailDoesNotExistException ex) {
        return warnResponse(HttpStatus.BAD_REQUEST, "Email no encontrado: {}", ex);
    }

    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCode(InvalidCodeException ex) {
        return warnResponse(HttpStatus.BAD_REQUEST, "Código de recuperación inválido: {}", ex);
    }

    @ExceptionHandler(CodeExpiredException.class)
    public ResponseEntity<Map<String, String>> handleCodeExpired(CodeExpiredException ex) {
        return warnResponse(HttpStatus.BAD_REQUEST, "Código de recuperación expirado: {}", ex);
    }

    @ExceptionHandler(SamePasswordException.class)
    public ResponseEntity<Map<String, String>> handleSamePassword(SamePasswordException ex) {
        return warnResponse(HttpStatus.BAD_REQUEST, "Intento de reutilización de contraseña: {}", ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return warnResponse(HttpStatus.BAD_REQUEST, "Argumento ilegal: {}", ex);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        log.error("Error de runtime no controlado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Validación fallida: campos={}", errors.keySet());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, String>> warnResponse(HttpStatus status, String logMsg, Exception ex) {
        log.warn(logMsg, ex.getMessage());
        return ResponseEntity.status(status).body(Map.of("message", ex.getMessage()));
    }
}
