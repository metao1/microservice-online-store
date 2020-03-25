package com.metao.product.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Data
@SuperBuilder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AutoAwareItemDTO implements Serializable {

    private static final long serialVersionUID = 343234890377423559L;

    private String asin;

    private String createdBy;

    private String modifiedBy;

    private LocalDate createdAt;

    private LocalDate modifiedAt;

}
