package com.styloflow.shared.domain.model;

import java.util.List;
import java.util.function.Function;

/** A page of results, independent of Spring Data. */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }
}
