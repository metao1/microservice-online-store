package com.metao.book.shared;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.reflect.Nullable;
import org.junit.jupiter.api.Test;

public class Reflect {

    public static class Packet {
        int cost;
        @Nullable
        TimeStamp stamp;

        public Packet() {
        }                        // required to read

        public Packet(int cost, TimeStamp stamp) {
            this.cost = cost;
            this.stamp = stamp;
        }
    }

    public static class TimeStamp {
        int hour = 0;
        int second = 0;

        public TimeStamp() {
        }                     // required to read

        public TimeStamp(int hour, int second) {
            this.hour = hour;
            this.second = second;
        }
    }


    @Test
    public void main() throws Exception {
        //Instantiating generated emp class
        Schema avroHttpRequest = SchemaBuilder.record("GetProductEvent")
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