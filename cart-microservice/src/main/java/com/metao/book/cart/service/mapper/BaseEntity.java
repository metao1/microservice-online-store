package com.metao.book.cart.service.mapper;

import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
public interface BaseEntity extends Serializable {

    Long getCreatedOn();

    void setCreatedOn(Long createdOn);

    Long getUpdatedOn();

    void setUpdatedOn(Long updatedOn);
}
