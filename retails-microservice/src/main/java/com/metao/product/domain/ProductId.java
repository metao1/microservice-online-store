package com.metao.product.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.metao.ddd.shared.domain.base.DomainObjectId;

public class ProductId extends DomainObjectId {
    
    @JsonCreator
    public ProductId(String uuid) {
        super(uuid);
    }
}
