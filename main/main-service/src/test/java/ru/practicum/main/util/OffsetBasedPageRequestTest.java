package ru.practicum.main.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OffsetBasedPageRequestTest {

    @Test
    void getOffset_returnsFromParameter() {
        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(5, 10, Sort.unsorted());
        assertEquals(5L, pageable.getOffset());
        assertEquals(10, pageable.getPageSize());
        assertEquals(0, pageable.getPageNumber());
        assertEquals(true, pageable.hasPrevious());
    }

    @Test
    void navigationMethods_adjustOffset() {
        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(10, 5, Sort.by("id"));
        assertEquals(15L, pageable.next().getOffset());
        assertEquals(5L, pageable.previousOrFirst().getOffset());
        assertEquals(0L, pageable.first().getOffset());
        assertEquals(20L, pageable.withPage(4).getOffset());
    }
}
