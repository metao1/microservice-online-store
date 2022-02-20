package com.metao.product.application.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.metao.ddd.finance.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class ProductDTO implements Serializable {

    @Pattern(regexp = "^[0-9]{10}")
    String asin;

    @Min(3)
    @Max(1200)
    String description;

    @Min(3)
    @Max(255)
    String title;

    @Length(max = 255)
    @Pattern(regexp = "(http|https):\\/\\/localhost:8080\\/(\\w|[_=-])*[\\.][\\w]+")
    String imageUrl;

    @Min(1)
    @Max(20000)
    Double price;

    Currency currency;

    @NotNull
    List<CategoryDTO> categories;
}
