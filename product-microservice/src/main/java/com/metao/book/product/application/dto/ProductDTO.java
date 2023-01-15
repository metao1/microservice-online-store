package com.metao.book.product.application.dto;

import com.metao.book.shared.domain.financial.Currency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO implements Serializable {

    @Pattern(regexp = "^\\d{10}", message = "asin format is wrong")
    private String asin;

    @Length(min = 1, max = 1250)
    private String description;

    @Length(min = 1, max = 255)
    private String title;

    @Length(min = 1, max = 255)
    @Pattern(regexp = "(http(s?):)([/|.|\\w|])*\\.(?:jpg|gif|png)", message = "url format is wrong")
    private String imageUrl;

    @NotNull
    @Min(1)
    @Max(20000)
    private BigDecimal price;

    @NotNull
    private Currency currency;

    @NotNull
    @Min(1)
    private BigDecimal volume;

    private Set<CategoryDTO> categories;

}
