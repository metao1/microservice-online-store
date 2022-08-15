package com.metao.book.retails.infrastructure.mapper;

public interface DTOMapper<T, U> {

    U convertToDto(T val);

}
