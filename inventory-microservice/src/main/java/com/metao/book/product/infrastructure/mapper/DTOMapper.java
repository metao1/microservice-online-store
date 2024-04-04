package com.metao.book.product.infrastructure.mapper;

public interface DTOMapper<T, U> {

    U convertToDto(T val);

}
