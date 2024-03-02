package com.metao.book.order.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.metao.book.shared.domain.base.DomainObjectId;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class OrderId extends DomainObjectId {

    @JsonCreator
    public OrderId(String uuid) {
        super(uuid);
    }
}
