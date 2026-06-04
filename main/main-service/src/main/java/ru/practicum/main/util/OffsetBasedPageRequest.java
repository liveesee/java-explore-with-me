package ru.practicum.main.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetBasedPageRequest implements Pageable {
    private final int offset;
    private final int pageSize;
    private final Sort sort;

    public OffsetBasedPageRequest(int offset, int pageSize) {
        this(offset, pageSize, Sort.unsorted());
    }

    public OffsetBasedPageRequest(int offset, int pageSize, Sort sort) {
        this.offset = offset;
        this.pageSize = pageSize;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }

    @Override
    public int getPageNumber() {
        return offset / pageSize;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetBasedPageRequest((int) getOffset() + getPageSize(), getPageSize(), getSort());
    }

    @Override
    public Pageable previousOrFirst() {
        int newOffset = (int) getOffset() - getPageSize();
        return new OffsetBasedPageRequest(Math.max(newOffset, 0), getPageSize(), getSort());
    }

    @Override
    public Pageable first() {
        return new OffsetBasedPageRequest(0, getPageSize(), getSort());
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetBasedPageRequest(pageNumber * getPageSize(), getPageSize(), getSort());
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
}
