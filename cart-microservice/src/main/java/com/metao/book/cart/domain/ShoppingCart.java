package com.metao.book.cart.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "shopping_cart")
@EqualsAndHashCode
public class ShoppingCart {

    @Id
    @Column(name = "cart_key")
    private String cartKey;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "asin")
    private String asin;

    @Column(name = "time_added")
    private String time_added;

    @Column(name = "quantity")
    private int quantity;

}