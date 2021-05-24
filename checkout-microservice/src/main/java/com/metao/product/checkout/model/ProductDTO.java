package com.metao.product.checkout.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize
public class ProductDTO implements Serializable {

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
}
