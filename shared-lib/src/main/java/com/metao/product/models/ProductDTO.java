package com.metao.product.models;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Set;

@Data
@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ProductDTO {

    String asin;

    String description;

    String title;

    String imageUrl;

    Double price;

    Integer numReviews;

    Double numStars;

    Double avgStars;

    RelatedDTO related;

    Set<String> categories;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class ProductDTOBuilder {
    }
}
