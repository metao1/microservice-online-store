package com.metao.product.application.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

@Data
@Builder
public class CategoryDTO implements Serializable {

    @Min(1)
    @Max(12)
    private String category;

}
