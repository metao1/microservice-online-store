package com.metao.product.checkout.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutStatus {

    public static final String SUCCESS = "SUCCESS";

    public static final String FAILURE = "FAILURE";

    String status;

    String orderNumber;

    String orderDetails;
}