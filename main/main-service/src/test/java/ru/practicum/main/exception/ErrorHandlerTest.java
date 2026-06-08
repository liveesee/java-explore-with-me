package ru.practicum.main.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.main.dto.ApiError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleNotFound_returns404() {
        ResponseEntity<ApiError> response = errorHandler.handleNotFound(
                new NotFoundException("Event with id=1 was not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND", response.getBody().getStatus());
    }

    @Test
    void handleBadRequest_returns400() {
        ResponseEntity<ApiError> response = errorHandler.handleBadRequest(
                new BadRequestException("Incorrectly made request."));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleForbidden_returns409WithForbiddenStatusField() {
        ResponseEntity<ApiError> response = errorHandler.handleForbidden(
                new ForbiddenOperationException("Conditions not met"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("FORBIDDEN", response.getBody().getStatus());
    }

    @Test
    void handleConflict_returns409() {
        ResponseEntity<ApiError> response = errorHandler.handleConflict(
                new ConflictException("The category is not empty"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleDataIntegrity_returns409() {
        ResponseEntity<ApiError> response = errorHandler.handleDataIntegrity(
                new DataIntegrityViolationException("duplicate key"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleValidation_returns400WithFieldMessage() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "name", "bad", false, null, null, "must not be blank"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ApiError> response = errorHandler.handleValidation(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().getMessage());
    }

    @Test
    void handleTypeMismatch_returns400() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "userId", null, new NumberFormatException("For input string"));
        ResponseEntity<ApiError> response = errorHandler.handleTypeMismatch(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
