package com.metao.book.product.application.model;

import java.time.Instant;
import org.apache.avro.reflect.AvroDoc;

@AvroDoc("""
    Metadata is used for internal services communication only.
    It keeps a unique key for partitioning and the timestamp, when event happened,
    this is important when we change the key inside the stream i.e. while repartitioning or when we need
    stateful operations such as constructing aggregations, group, merge or reduce events.
    """)
public record Metadata(@AvroDoc("The key used by Kafka for key partitioning") String key,
                       @AvroDoc("Timestamp used when an event occurred") Instant timestamp) {}
