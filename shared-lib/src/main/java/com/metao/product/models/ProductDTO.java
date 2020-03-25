package com.metao.product.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
@JsonDeserialize(builder = ProductDTO.ProductDTOBuilder.class)
public class ProductDTO extends AutoAwareItemDTO {

    private String description;

    private String brand;

    private String title;

    private Set<String> categories;

    private String imageUrl;

    private Double price;

}
