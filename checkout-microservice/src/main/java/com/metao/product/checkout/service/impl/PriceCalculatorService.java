package com.metao.product.checkout.service.impl;

import com.metao.product.checkout.clients.ProductCatalogRestClient;
import com.metao.product.models.ProductDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PriceCalculatorService {

    private final ProductCatalogRestClient productCatalogRestClient;

    public Double calculateTotalPriceInCart(Map<String, Integer> products) {
        return products.entrySet()
                .stream()
                .collect(PriceCalculator::new, (k, v) -> k.accept(v.getValue(), lookupPrice(v.getKey())), PriceCalculator::combine)
                .getTotal();
    }

    private double lookupPrice(@NonNull final String productKey) {
        ProductDTO productDetails = productCatalogRestClient.getProductDetails(productKey);
        if (productDetails != null) {
            return productDetails.getPrice();
        }
        return 0;
    }

    private static final class PriceCalculator implements BiConsumer<Integer, Double> {

        private double price;
        private int quantity;
        private double total;

        public void combine(PriceCalculator priceCalculator) {
            priceCalculator.total = total;
        }

        public double getTotal() {
            return total;
        }

        @Override
        public void accept(Integer quantity, Double price) {
            this.total += price * quantity;
        }
    }
}
