package com.metao.product.domain.image;

import com.metao.ddd.base.ValueObject;

import lombok.EqualsAndHashCode;

public record Image(String url) implements ValueObject {

}
