package com.metao.book.order.application.service;

//@Slf4j
//@Server
//public class KafkaOrderConsumer {
//
//    private CountDownLatch latch = new CountDownLatch(10);
//    private String payload = null;
//
//    @KafkaListener(id = "orders", topics = "order", groupId = "payment")
//    public void onEvent(ConsumerRecord<String, OrderEvent> record) {
//        log.info("Consumed message -> {}", record.value());
//        latch.countDown();
//    }
//
//    public CountDownLatch getLatch() {
//        return latch;
//    }
//
//    public String getPayload() {
//        return payload;
//    }
//}