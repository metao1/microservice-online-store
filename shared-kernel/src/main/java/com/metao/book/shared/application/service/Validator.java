package com.metao.book.shared.application.service;

public interface Validator<T> {
    void validate(T t) throws RuntimeException;
}
