package com.metao.book.product.application.model;

import org.apache.avro.reflect.AvroDoc;

@AvroDoc("""
    User can be identified by it's id
    """)
public record User(@AvroDoc("The user id") String userId) {
}
