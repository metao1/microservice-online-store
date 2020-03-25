package com.metao.product.retails.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.time.LocalDate;

@Getter
@Embeddable
@Data
@SuperBuilder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class AutoAwareItemEntity {

    private String createdBy;

    private String modifiedBy;

    private LocalDate createdAt;

    private LocalDate modifiedAt;

}
