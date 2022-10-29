package com.metao.book.order.infrastructure;

import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

public interface KafkaListenableCallback<K, V> extends ListenableFutureCallback<SendResult<K, V>> {

}
