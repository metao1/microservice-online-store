package com.metao.book.product.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.metao.book.shared.domain.base.DomainObjectId;
import org.springframework.lang.NonNull;

public class ProductId extends DomainObjectId {

    @JsonCreator
    public ProductId(@NonNull String asin) {
        super(asin);
    }

}
