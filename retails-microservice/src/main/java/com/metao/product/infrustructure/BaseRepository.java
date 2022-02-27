package com.metao.product.infrustructure;

public interface BaseRepository<T> {
    void save(T t);
}
