package com.metao.book.product.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class CategoryDTO implements Serializable {

    @Min(1)
    @Max(12)
    @NonNull
    private String category;

}
