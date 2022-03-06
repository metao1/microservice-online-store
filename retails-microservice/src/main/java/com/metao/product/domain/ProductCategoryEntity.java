package com.metao.product.domain;

import com.metao.ddd.base.AbstractEntity;
import com.metao.ddd.base.DomainObjectId;
import com.metao.product.domain.category.Category;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "product_category")
public class ProductCategoryEntity extends AbstractEntity<ProductCategoryId> {

    private Category category;

    public ProductCategoryEntity(Category category) {
        super(DomainObjectId.randomId(ProductCategoryId.class));
        this.category = category;
    }

    public Category getCategory() {
        return  category;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this==obj) return true;
        if(obj ==null || getClass() != obj.getClass()) return false;
        ProductCategoryEntity cat = (ProductCategoryEntity) obj;
        return (this.category!=null ? !this.category.equals(cat.getCategory()): cat.getCategory()!=null);
    }

    @Override
    public int hashCode() {
        int result = category!=null ? category.category().hashCode(): 0;        
        return 31 * result;
    }
}
