package com.metao.product.checkout.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "orders")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEntity {

    @Id
    @Column(name = "order_id")
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "order_details")
    private String orderDetails;

    @Column(name = "order_time")
    private long orderTime;

    @Column(name = "order_total")
    private Double orderTotal;
}
