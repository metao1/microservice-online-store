package com.metao.product.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductDTO extends AutoAwareItemDTO {

    private String description;

    private String brand;

    private String title;

    private Set<String> categories;

    private String imageUrl;

    private Double price;

}
