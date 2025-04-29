package com.metao.book.product.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.metao.book.product.application.config.CurrencyDeserializer;
import com.metao.book.product.domain.category.dto.CategoryDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record ProductDTO(

    @NotNull
    @Pattern(regexp = "^\\w{10}", message = "asin format is wrong")
    String asin,

    @NotNull
    @Length(min = 1, max = 2500)
    String title,

    @Length(min = 1, max = 10_485_760)
    String description,

    @JsonProperty("imageUrl")
    @Length(min = 1, max = 255)
    @Pattern(regexp = "(http(s?):)([/|.|\\w|])*\\.(?:jpg|gif|png)", message = "url format is wrong")
    String imageUrl,

    @NotNull
    BigDecimal price,

    @NotNull
    @JsonDeserialize(using = CurrencyDeserializer.class)
    Currency currency,

    @NotNull
    BigDecimal volume,

    @JsonProperty("bought_together")
    List<String> boughtTogether,

    Set<CategoryDTO> categories) {

}
