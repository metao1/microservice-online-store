package com.metao.book.cart.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartKey implements Serializable {

    private String userId;

    private String asin;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingCartKey that = (ShoppingCartKey) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(asin, that.asin);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
