package com.metao.product.retails.model;

import lombok.*;

@Data
@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ProductCategoriesDTO {

    String id;

    String categories;
}
