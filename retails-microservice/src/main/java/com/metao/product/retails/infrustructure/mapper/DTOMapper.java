package com.metao.product.retails.infrustructure.mapper;

public interface DTOMapper<T, U> {

    U convertToDto(T val);

}
