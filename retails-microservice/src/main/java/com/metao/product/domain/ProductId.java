package com.metao.product.domain;

import com.metao.ddd.shared.domain.base.DomainObjectId;

public class ProductId extends DomainObjectId {
    public ProductId(String uuid) {
        super(uuid);
    }
}
