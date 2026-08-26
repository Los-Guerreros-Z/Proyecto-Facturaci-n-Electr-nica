package api.controller;

import infraestructure.integration.IntegracionExternaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(e -> e.getField() + ": " + e.getDefaultMessage()).orElse("Solicitud inválida");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({IllegalArgumentException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> badRequest(Exception ex) {
        String message = ex instanceof BadCredentialsException ? "Credenciales inválidas" : ex.getMessage();
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> forbidden(SecurityException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }


    @ExceptionHandler(IntegracionExternaException.class)
    public ResponseEntity<Map<String, Object>> integracionExterna(IntegracionExternaException ex) {
        return error(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generic(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servicio");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
