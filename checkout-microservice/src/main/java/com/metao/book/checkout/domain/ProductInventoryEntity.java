package com.metao.book.checkout.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_inventory")
public class ProductInventoryEntity {

    @Id
    @Column(name = "asin")
    private String asin;

    @Column(name = "amount_available")
    private Double amountAvailable;

    @Column(name = "amount_reserved")
    private Double amountReserved; // quantity - reservedQuantity

}