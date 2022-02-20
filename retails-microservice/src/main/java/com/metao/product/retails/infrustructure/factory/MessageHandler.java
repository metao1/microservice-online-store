package com.metao.product.retails.infrustructure.factory;

import com.metao.product.retails.domain.product.event.CreateProductEvent;
import org.springframework.lang.NonNull;

public interface MessageHandler {
    void onCreateProductEvent(@NonNull CreateProductEvent createProductEvent);
}
