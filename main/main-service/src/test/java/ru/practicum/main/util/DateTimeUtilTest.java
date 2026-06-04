package ru.practicum.main.util;

import org.junit.jupiter.api.Test;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ForbiddenOperationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateTimeUtilTest {

    @Test
    void parse_validDateTime_returnsParsedValue() {
        LocalDateTime expected = LocalDateTime.of(2026, 6, 1, 12, 0, 0);
        assertEquals(expected, DateTimeUtil.parse("2026-06-01 12:00:00"));
    }

    @Test
    void parse_invalidFormat_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> DateTimeUtil.parse("2026-06-01"));
    }

    @Test
    void format_null_returnsNull() {
        assertNull(DateTimeUtil.format(null));
    }

    @Test
    void format_validDateTime_returnsFormattedString() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 1, 12, 0, 0);
        assertEquals("2026-06-01 12:00:00", DateTimeUtil.format(dateTime));
    }

    @Test
    void validateEventDateForUser_tooSoon_throwsBadRequest() {
        LocalDateTime soon = LocalDateTime.now().plusHours(1);
        assertThrows(BadRequestException.class,
                () -> DateTimeUtil.validateEventDateForUser(soon));
    }

    @Test
    void validateEventDateForAdmin_beforePublishedPlusHour_throwsForbidden() {
        LocalDateTime publishedOn = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime eventDate = publishedOn.plusMinutes(30);
        assertThrows(ForbiddenOperationException.class,
                () -> DateTimeUtil.validateEventDateForAdmin(eventDate, publishedOn));
    }
}
