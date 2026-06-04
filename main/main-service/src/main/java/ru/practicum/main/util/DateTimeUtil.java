package ru.practicum.main.util;

import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ForbiddenOperationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateTimeUtil {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DATE_FORMAT_MESSAGE = "Invalid date format. Expected: yyyy-MM-dd HH:mm:ss";

    private DateTimeUtil() {
    }

    public static LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BadRequestException(DATE_FORMAT_MESSAGE);
        }
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : FORMATTER.format(dateTime);
    }

    public static void validateEventDateForUser(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + eventDate);
        }
    }

    public static void validateEventDateForAdmin(LocalDateTime eventDate, LocalDateTime publishedOn) {
        if (publishedOn != null && eventDate.isBefore(publishedOn.plusHours(1))) {
            throw new ForbiddenOperationException(
                    "Field: eventDate. Error: должно содержать дату, которая наступит не раньше, "
                            + "чем через час после публикации. Value: " + eventDate);
        }
    }
}
