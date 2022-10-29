package com.metao.book.order.infrastructure.mapper;

public interface DTOMapper<T, U> {

    U convertToDto(T val);

}
