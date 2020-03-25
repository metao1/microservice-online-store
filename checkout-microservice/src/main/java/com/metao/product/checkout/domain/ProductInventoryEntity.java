package com.metao.product.checkout.domain;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "product_inventory")
@Getter
public class ProductInventoryEntity {

    @Id
    @Column(name = "asin")
    private String id;

    @Column(name = "quantity")
    private Integer quantity;
}