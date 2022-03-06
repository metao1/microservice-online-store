package com.metao.product.domain.category;

import java.util.Objects;

import com.metao.ddd.base.ValueObject;

public class Category implements ValueObject {

        private String value;

        public Category(String category) {
                this.value = category;
        }

        public String category() {
                return value;
        }

        @Override
        public boolean equals(Object obj) {
                if(obj==null || obj!= this) return false;
                Category cat = (Category) obj;
                return cat.category().equals(value);
        }

        @Override
        public int hashCode() {
                int result = value != null ? value.hashCode() : 0;
                return 31 * result;
        }
}
