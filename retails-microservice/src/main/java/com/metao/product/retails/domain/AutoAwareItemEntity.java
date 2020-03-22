package com.metao.product.retails.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Table;
import java.time.Instant;

@Data
@Table(name = "auto_item")
@AllArgsConstructor
@NoArgsConstructor
public class AutoAwareItemEntity {

    @Id
    @Column(name="id")
    private String id;

    private String createdBy;

    private String modifiedBy;

    private Instant createdAt;

    private Instant modifiedAt;

}
