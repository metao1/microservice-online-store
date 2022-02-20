package com.metao.product.infrustructure.factory;

import com.metao.product.domain.event.CreateProductEvent;
import org.springframework.lang.NonNull;

public interface MessageHandler {
    void onCreateProductEvent(@NonNull CreateProductEvent createProductEvent);
}
