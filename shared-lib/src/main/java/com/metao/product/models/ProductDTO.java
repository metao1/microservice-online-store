package com.metao.product.models;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Set;

@Data
@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ProductDTO {

    @Pattern(regexp = "^[0-9]{10}")
    String asin;

    @Length(min = 0, max = 1900)
    String description;

    @Length(min = 3, max = 255)
    String title;

    @Length(max = 255)
    String imageUrl;

    @Min(1)
    @Max(20000)
    Double price;

    Integer numReviews;

    Double numStars;

    Double avgStars;

    RelatedDTO related;

    @NotNull
    Set<String> categories;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class ProductDTOBuilder {
    }
}
