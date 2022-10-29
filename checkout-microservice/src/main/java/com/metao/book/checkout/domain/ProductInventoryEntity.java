package com.metao.book.checkout.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "product_inventory")
public class ProductInventoryEntity {

    @Id
    @Column(name = "asin")
    private String asin;

    @Column(name = "amount_available")
    private Integer amountAvailable;

    @Column(name = "amount_reserved")
    private long amountReserved; // quantity - reservedQuantity

}