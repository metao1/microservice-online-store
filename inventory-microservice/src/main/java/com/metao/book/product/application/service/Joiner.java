package com.metao.book.product.application.service;

public interface Joiner<O, D, S> {

    S join(O input, D output);
}
