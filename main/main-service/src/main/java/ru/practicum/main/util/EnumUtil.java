package ru.practicum.main.util;

import ru.practicum.main.exception.BadRequestException;

public final class EnumUtil {
    private EnumUtil() {
    }

    public static <E extends Enum<E>> E parse(Class<E> type, String value) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BadRequestException("Incorrectly made request.");
        }
    }
}
