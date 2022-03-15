package com.metao.book.order.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.metao.book.shared.domain.base.DomainObjectId;

public class OrderId extends DomainObjectId {

    @JsonCreator
    protected OrderId(String uuid) {
        super(uuid);
    }
}
