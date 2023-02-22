package com.metao.book.cart.service.mapper;

import java.io.Serializable;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseDTO implements Serializable {

    protected Long createdOn;
    protected Long updatedOn;
}
