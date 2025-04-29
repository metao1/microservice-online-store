package com.metao.book.product.application.service;

import com.metao.book.product.event.ProductCreatedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
public class EventValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isInstance(ProductCreatedEvent.class);
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors errors) {
        ProductCreatedEvent productCreatedEvent = (ProductCreatedEvent) target;
        if (productCreatedEvent.getCurrency().isEmpty()) {
            errors.rejectValue("currency", "currency.invalid");
        }
        if (productCreatedEvent.getPrice() < 0) {
            errors.rejectValue("price", "price.invalid");
        }
    }
}
