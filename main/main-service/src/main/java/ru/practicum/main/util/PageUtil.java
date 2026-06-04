package ru.practicum.main.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtil {
    private PageUtil() {
    }

    public static Pageable createPageable(int from, int size) {
        return new OffsetBasedPageRequest(from, size);
    }

    public static Pageable createPageable(int from, int size, Sort sort) {
        return new OffsetBasedPageRequest(from, size, sort);
    }
}
