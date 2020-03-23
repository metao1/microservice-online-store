package com.metao.product.retails.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Table(name = "product")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ProductEntity extends BaseEntity {

    @Id
    @Column(name = "asin")
    protected String id;

    private String brand;

    @Column(name = "categories")
    @ElementCollection(targetClass = String.class)
    @CollectionTable(name = "product_categories", joinColumns = @JoinColumn(name = "id") )
    private Set<String> categories = new HashSet<>();

    private String description;

    @Column(name = "imgurl")
    private String imageUrl;

    private Double price;
}
