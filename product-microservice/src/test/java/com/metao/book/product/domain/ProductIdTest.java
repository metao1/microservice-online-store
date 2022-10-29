package com.metao.book.product.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ProductIdTest {

    @Test
    public void twoProductAreSimilarUsingSameId() {
        var prd1 = new ProductId("123123123");
        var prd2 = new ProductId("123123123");
        Assertions.assertEquals(prd1, prd2);
    }

}