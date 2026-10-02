package com.styloflow.shared.infrastructure.persistence;

import com.styloflow.shared.domain.model.PageResult;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public final class PageResults {

    private PageResults() {}

    public static <E, T> PageResult<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResult<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
