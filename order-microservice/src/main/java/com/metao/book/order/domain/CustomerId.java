package com.metao.book.order.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.metao.book.shared.domain.base.DomainObjectId;

public class CustomerId extends DomainObjectId {

    @JsonCreator
    protected CustomerId(String uuid) {
        super(uuid);
    }
}
