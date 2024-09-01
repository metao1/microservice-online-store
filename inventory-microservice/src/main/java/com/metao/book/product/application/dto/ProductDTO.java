package com.metao.book.product.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record ProductDTO(

        @NotNull
        @Pattern(regexp = "^\\d{10}", message = "asin format is wrong")
        String asin,

        @Length(min = 1, max = 1250)
        String description,

        @NotNull
        @Length(min = 1, max = 255)
        String title,

        @JsonProperty("image_url")
        @Length(min = 1, max = 255)
        @Pattern(regexp = "(http(s?):)([/|.|\\w|])*\\.(?:jpg|gif|png)", message = "url format is wrong")
        String imageUrl,

        @NotNull
        BigDecimal price,

        @NotNull
        Currency currency,

        @NotNull
        BigDecimal volume,

        Set<CategoryDTO> categories) {

}
