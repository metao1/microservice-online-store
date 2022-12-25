package com.metao.book.product.application.service;

import com.metao.book.product.application.model.Metadata;
import java.time.Instant;

public interface MetadataHandler {

    default Metadata handleMetadata(String key, Instant timestamp) {
        return new Metadata(key, timestamp);
    }
}
