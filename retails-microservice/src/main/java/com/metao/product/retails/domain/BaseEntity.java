package com.metao.product.retails.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@SuperBuilder
public class BaseEntity {

    protected String createdBy;

    protected String modifiedBy;

    protected Date createdAt;

    protected Date modifiedAt;

}
