package ru.practicum.stats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {
    private static final String DATE_FORMAT_MESSAGE = "Invalid date format. Expected: yyyy-MM-dd HH:mm:ss";

    @ExceptionHandler({BadRequestException.class, DateTimeParseException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(RuntimeException exception) {
        String message = exception instanceof DateTimeParseException
                ? DATE_FORMAT_MESSAGE
                : exception.getMessage();
        return Map.of("error", message);
    }
}
