package com.metao.book.shared.application.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record OffsetBasedPageRequest(int offset, int limit) implements Pageable {

    public OffsetBasedPageRequest {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset index must not be less than zero");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must not be less than one");
        }
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted();
    }

    @Override
    public Pageable next() {
        return new OffsetBasedPageRequest((int) (getOffset() + getPageSize()), getPageSize());
    }

    public Pageable previous() {
        return hasPrevious() ? new OffsetBasedPageRequest((int) (getOffset() - getPageSize()), getPageSize())
            : this;
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @Override
    public Pageable first() {
        return new OffsetBasedPageRequest(0, getPageSize());
    }

    @Override
    public boolean hasPrevious() {
        return offset >= limit;
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetBasedPageRequest(pageNumber * getPageSize(), getPageSize());
    }
}
