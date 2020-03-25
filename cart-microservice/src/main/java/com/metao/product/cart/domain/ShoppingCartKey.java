package com.metao.product.cart.domain;

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
@Table(name = "shopping_cart_key")
@EqualsAndHashCode
public class ShoppingCartKey {

    @Id
    @Column(name = "user_id")
    private String id;

    @Column(name = "asin")
    private String asin;

}
