package ru.practicum.main.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.main.dto.ApiError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@RestControllerAdvice
public class ErrorHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException exception) {
        return buildError(HttpStatus.NOT_FOUND, "The required object was not found.", exception.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException exception) {
        return buildError(HttpStatus.BAD_REQUEST, "Incorrectly made request.", exception.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenOperationException exception) {
        return buildError(HttpStatus.CONFLICT, "For the requested operation the conditions are not met.",
                exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException exception) {
        return buildError(HttpStatus.CONFLICT, "For the requested operation the conditions are not met.",
                exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException exception) {
        return buildError(HttpStatus.CONFLICT, "Integrity constraint has been violated.", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> String.format("Field: %s. Error: %s. Value: %s",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .orElse("Incorrectly made request.");
        return buildError(HttpStatus.BAD_REQUEST, "Incorrectly made request.", message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String message = String.format("Failed to convert value of type %s to required type %s; nested exception is %s",
                exception.getValue() != null ? exception.getValue().getClass().getName() : "null",
                exception.getRequiredType() != null ? exception.getRequiredType().getName() : "unknown",
                exception.getMostSpecificCause().getMessage());
        return buildError(HttpStatus.BAD_REQUEST, "Incorrectly made request.", message);
    }

    private ResponseEntity<ApiError> buildError(HttpStatus httpStatus, String reason, String message) {
        return buildError(httpStatus, reason, message, httpStatus);
    }

    private ResponseEntity<ApiError> buildError(HttpStatus httpStatus, String reason, String message,
                                                HttpStatus statusField) {
        ApiError error = ApiError.builder()
                .status(statusField.name())
                .reason(reason)
                .message(message)
                .timestamp(FORMATTER.format(LocalDateTime.now()))
                .errors(Collections.emptyList())
                .build();
        return ResponseEntity.status(httpStatus).body(error);
    }
}
