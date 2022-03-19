package com.metao.book.retails.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.metao.book.shared.domain.base.DomainObjectId;

public class ProductId extends DomainObjectId {

    @JsonCreator
    public ProductId(String uuid) {
        super(uuid);
    }
}
