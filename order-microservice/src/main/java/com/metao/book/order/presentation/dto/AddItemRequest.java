package com.metao.book.order.presentation.dto;

import lombok.Data;

@Data
public class AddItemRequest {

    private String productId;
    private String productName;
    private int quantity;
    private double unitPrice;
}