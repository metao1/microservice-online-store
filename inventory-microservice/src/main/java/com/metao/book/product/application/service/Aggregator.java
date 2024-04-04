package com.metao.book.product.application.service;

public interface Aggregator<O, D> {

    D apply(O order, D total);
}
