package com.metao.product.retails.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product")
@EqualsAndHashCode(callSuper = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProductEntity extends AutoAwareItemEntity {

    @Id
    @Column(name = "asin")
    private String id;

    private String title;

    @Column(length = 1900)
    private String description;

    @Column(name = "imgurl")
    private String imageUrl;

    private Double price;

    private Integer numReviews;

    private Double numStars;

    private Double avgStars;

//    @Embedded
//    private RelatedEntity related;

    @Column(name = "categories")
    @ManyToMany(targetEntity = ProductCategoryEntity.class, cascade = CascadeType.ALL)
    @JoinTable(name = "product_categories", joinColumns = {@JoinColumn(name = "asin")}, inverseJoinColumns = {@JoinColumn(name = "id")})
    private Set<ProductCategoryEntity> categories = new HashSet<>();

}
