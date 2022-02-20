package com.metao.product.infrustructure.mapper;

public interface DTOMapper<T, U> {

    U convertToDto(T val);

}
