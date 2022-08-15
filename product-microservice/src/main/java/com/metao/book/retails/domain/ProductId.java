package com.metao.book.retails.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.metao.book.shared.domain.base.DomainObjectId;

import java.io.Serializable;

public class ProductId extends DomainObjectId implements Serializable {

    @JsonCreator
    public ProductId(String asin) {
        super(asin);
    }

}
