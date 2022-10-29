package com.metao.book.product.application.dto;

import com.metao.book.shared.domain.financial.Currency;
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
import java.math.BigDecimal;
import java.util.Set;

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

    @Length(min = 1, max = 255)
    @Pattern(regexp = "(http(s?):)([/|.|\\w|-|])*\\.(?:jpg|gif|png)", message = "url format is wrong")
    private String imageUrl;

    @NotNull
    @Min(1)
    @Max(20000)
    private BigDecimal price;

    @NotNull
    private Currency currency;

    @NotNull
    private Set<CategoryDTO> categories;

}
