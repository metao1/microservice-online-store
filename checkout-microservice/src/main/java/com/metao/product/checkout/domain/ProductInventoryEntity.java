package com.metao.product.checkout.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "product_inventory")
@Getter
@Setter
public class ProductInventoryEntity {

    @Id
    @Column(name = "asin")
    private String id;

    @Column(name = "quantity")
    private Integer quantity;
}