package com.metao.product.retails.infrustructure;

public interface BaseRepository<T, U> {
    T save(T t);
}
