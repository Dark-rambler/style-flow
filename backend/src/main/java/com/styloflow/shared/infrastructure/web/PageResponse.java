package com.styloflow.shared.infrastructure.web;

import com.styloflow.shared.domain.model.PageResult;
import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PageResponse<T> of(PageResult<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.content()
                        .stream()
                        .map(mapper)
                        .toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }
}
