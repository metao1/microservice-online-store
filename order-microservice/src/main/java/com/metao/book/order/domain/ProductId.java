package com.metao.book.order.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.metao.book.shared.domain.base.DomainObjectId;

public class ProductId extends DomainObjectId {

    @JsonCreator
    protected ProductId(String uuid) {
        super(uuid);
    }
}
