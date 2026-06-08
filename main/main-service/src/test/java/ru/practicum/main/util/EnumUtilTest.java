package ru.practicum.main.util;

import org.junit.jupiter.api.Test;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.model.RequestStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnumUtilTest {

    @Test
    void parse_validValue_returnsEnum() {
        assertEquals(RequestStatus.CONFIRMED, EnumUtil.parse(RequestStatus.class, "CONFIRMED"));
    }

    @Test
    void parse_invalidValue_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> EnumUtil.parse(RequestStatus.class, "UNKNOWN"));
    }

    @Test
    void parse_nullValue_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> EnumUtil.parse(RequestStatus.class, null));
    }
}
