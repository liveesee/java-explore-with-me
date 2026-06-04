package ru.practicum.main.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PageUtilTest {

    @Test
    void createPageable_returnsOffsetBasedPageRequest() {
        Pageable pageable = PageUtil.createPageable(3, 15);
        assertInstanceOf(OffsetBasedPageRequest.class, pageable);
        assertEquals(3L, pageable.getOffset());
        assertEquals(15, pageable.getPageSize());
    }

    @Test
    void createPageable_withSort_appliesSort() {
        Sort sort = Sort.by("name").ascending();
        OffsetBasedPageRequest pageable = (OffsetBasedPageRequest) PageUtil.createPageable(0, 10, sort);
        assertEquals(sort, pageable.getSort());
    }
}
