package com.metao.product.util;

import java.util.Set;

import com.metao.ddd.finance.Currency;
import com.metao.ddd.finance.Money;
import com.metao.product.application.dto.CategoryDTO;
import com.metao.product.application.dto.ProductDTO;
import com.metao.product.domain.ProductEntity;
import com.metao.product.domain.category.CategoryEntity;
import com.metao.product.domain.image.Image;

public class ProductTestUtils {

        public static ProductEntity createProductEntity(){                
                var url = "http://example.com/image.jpg";
                var description = "description";
                var title = "title";
                var price = 12d;
                var currency = Currency.DLR;
                var category = new CategoryEntity("book");
                var pe = new ProductEntity(title, description, new Money(currency, price), new Image(url));
                pe.addCategory(category);
                return pe;
        }

        public static ProductDTO createProductDTO() {
                return ProductDTO
                                .builder()                                
                                .asin("1234567899")
                                .title("title")
                                .description("description")
                                .price(12d)
                                .currency(Currency.DLR)
                                .categories(Set.of(CategoryDTO.of("book")))
                                .imageUrl("http://example.com/image.jpg")
                                .build();
        }
        
}
