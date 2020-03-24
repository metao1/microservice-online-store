package com.metao.product.retails.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Embeddable;
import java.util.Date;

@Getter
@Embeddable
@Data
@SuperBuilder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AutoAwareItemEntity {

    private String createdBy;

    private String modifiedBy;

    private Date createdAt;

    private Date modifiedAt;

}
