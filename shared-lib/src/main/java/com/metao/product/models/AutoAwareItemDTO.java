package com.metao.product.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.metao.product.utils.EventDateDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

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

    @JsonDeserialize(using = EventDateDeserializer.class)
    private Date createdAt;

    @JsonDeserialize(using = EventDateDeserializer.class)
    private Date modifiedAt;

}
