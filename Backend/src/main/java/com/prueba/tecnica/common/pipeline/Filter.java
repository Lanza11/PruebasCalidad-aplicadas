package com.prueba.tecnica.common.pipeline;

public interface Filter<T> {
    T execute(T input);
}
