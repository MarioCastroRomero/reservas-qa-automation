package com.mariocastro.reservas.controller.advice;

import com.mariocastro.reservas.dto.ApiError;
import com.mariocastro.reservas.exception.ConflictException;
import com.mariocastro.reservas.exception.ForbiddenException;
import com.mariocastro.reservas.exception.NotFoundException;
import com.mariocastro.reservas.exception.ValidateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static ResponseEntity<ApiError> build(HttpStatus status, String error, List<String> details) {
        return new ResponseEntity<>(new ApiError(LocalDateTime.now(), status.value(), error, details), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Validation error", errors);
    }

    @ExceptionHandler({ValidateException.class, jakarta.validation.ValidationException.class,
            IllegalArgumentException.class, MethodArgumentTypeMismatchException.class,
            org.springframework.data.core.PropertyReferenceException.class})
    public ResponseEntity<ApiError> handleValidationExceptions(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Validation error", List.of(String.valueOf(ex.getMessage())));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Validation error", List.of("Malformed or unreadable request body"));
    }

    @ExceptionHandler({NotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ApiError> handleNotFoundException(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, "Resource not found", List.of(ex.getMessage()));
    }

    @ExceptionHandler({ConflictException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiError> handleConflict(RuntimeException ex) {
        String detail = ex instanceof ConflictException
                ? ex.getMessage()
                : "The resource was modified by someone else, please retry";
        return build(HttpStatus.CONFLICT, "Conflict", List.of(detail));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return build(HttpStatus.CONFLICT, "Conflict", List.of("The operation violates a data constraint"));
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ApiError> handleForbidden(RuntimeException ex) {
        String detail = ex instanceof ForbiddenException
                ? ex.getMessage()
                : "You do not have permission to perform this action";
        return build(HttpStatus.FORBIDDEN, "Forbidden", List.of(detail));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid credentials", List.of("Email or password is incorrect"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication error", List.of(ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", List.of(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", List.of("Unexpected error"));
    }
}
