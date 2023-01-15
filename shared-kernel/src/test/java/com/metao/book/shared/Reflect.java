package com.metao.book.shared;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.jupiter.api.Test;

public class Reflect {

    @Test
    public void main() {
        //Instantiating generated emp class
        Schema avroHttpRequest = SchemaBuilder.record("ProductsResponseEvent")
            .namespace("com.metao.book.shared")
            .fields()
            .requiredString("productId")
            .name("occurredOn")
            .type()
            .longType()
            .noDefault()
            .endRecord();
        System.out.println(avroHttpRequest.toString());
    }

}