package com.metao.book.checkout.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class ProductApiErrorDTO implements Serializable {

    private static final long serialVersionUID = -6646128061564873843L;

    private String message;
}
