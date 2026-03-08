package com.prueba.tecnica.common.pipeline;

import java.util.ArrayList;
import java.util.List;

public class Pipeline<T> {
    private final List<Filter<T>> filters = new ArrayList<>();

    public Pipeline<T> addFilter(Filter<T> filter) {
        filters.add(filter);
        return this;
    }

    public T process(T input) {
        T result = input;
        for (Filter<T> filter : filters) {
            result = filter.execute(result);
        }
        return result;
    }
}
