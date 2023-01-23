package com.metao.book.checkout.domain;

import com.metao.book.checkout.application.model.CustomerId;
import com.metao.book.shared.domain.base.AbstractEntity;
import com.metao.book.shared.domain.base.DomainObjectId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Entity(name = "customer")
@Table(name = "customer_entity")
public class CustomerEntity extends AbstractEntity<CustomerId> {

    /**
     * Default constructor
     */
    public CustomerEntity() {
        super(DomainObjectId.randomId(CustomerId.class));
    }

    private String name;

    @Column(name = "amount_available")
    private BigDecimal amountAvailable;

    @Column(name = "amount_reserved")
    private BigDecimal amountReserved;

}
