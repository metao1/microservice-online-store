package com.metao.product.application.dto;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.metao.ddd.finance.Currency;

import org.hibernate.validator.constraints.Length;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO implements Serializable {

    @Pattern(regexp = "^[0-9]{10}")
    private String asin;

    @Min(3)
    @Max(1200)
    private String description;

    @Min(3)
    @Max(255)
    private String title;

    @Length(max = 255)
    @Pattern(regexp = "^(https?):\\[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
    private String imageUrl;

    @Min(1)
    @Max(20000)
    private Double price;

    private Currency currency;

    @NotNull
    private List<CategoryDTO> categories;
}
