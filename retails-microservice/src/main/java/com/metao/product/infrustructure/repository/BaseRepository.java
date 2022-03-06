package com.metao.product.infrustructure.repository;

public interface BaseRepository<T> {
    void save(T t);
}
