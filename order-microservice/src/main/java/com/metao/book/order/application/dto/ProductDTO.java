package com.metao.book.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO implements Serializable {

    @Pattern(regexp = "^[0-9]{10}", message = "asin format is wrong")
    private String asin;


}