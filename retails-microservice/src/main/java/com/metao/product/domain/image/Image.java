package com.metao.product.domain.image;

import java.util.Objects;

import com.metao.ddd.base.ValueObject;

public class Image implements ValueObject {

        private String url;

        public Image(String url) {
                this.url = Objects.requireNonNull(url, "uuid must not be null");
        }

        public String url() {
                return url;
        }

        @Override
        public boolean equals(Object obj) {
                if (obj == null) {
                        return false;
                }
                return (((Image) obj).url().equals(url));
        }
        
        @Override
        public int hashCode() {
                return Objects.hash(url);                
        }
}
