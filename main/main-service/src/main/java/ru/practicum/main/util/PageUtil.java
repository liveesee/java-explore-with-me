package ru.practicum.main.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.main.exception.BadRequestException;

public final class PageUtil {
    private PageUtil() {
    }

    public static Pageable createPageable(int from, int size) {
        validatePagination(from, size);
        return new OffsetBasedPageRequest(from, size);
    }

    public static Pageable createPageable(int from, int size, Sort sort) {
        validatePagination(from, size);
        return new OffsetBasedPageRequest(from, size, sort);
    }

    private static void validatePagination(int from, int size) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Incorrectly made request.");
        }
    }
}
