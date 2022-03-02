package com.metao.product.application.dto;

import java.io.Serializable;
import java.util.Set;

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

    @Pattern(regexp = "^[0-9]{10}", message = "asin format is wrong")
    private String asin;
    
    @Length(min = 1, max = 1250)
    private String description;

    @Length(min = 1, max = 255)
    private String title;

    @Length(min=1, max = 255)
    @Pattern(regexp = "(http(s?):)([/|.|\\w|-|])*\\.(?:jpg|gif|png)", message = "url format is wrong")
    private String imageUrl;

    @NotNull
    @Min(1)
    @Max(20000)
    private Double price;

    @NotNull
    private Currency currency;

    @NotNull
    private Set<CategoryDTO> categories;

}
