package com.metao.book.retails.infrustructure.mapper;

public interface DTOMapper<T, U> {

    U convertToDto(T val);

}
