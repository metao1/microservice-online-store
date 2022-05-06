package order.presentation;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext
@ContextConfiguration(classes = KafkaOrderConsumerConfiguration.class)
public class OrderControllerTest {
//
//        private static Random RAND = new Random();
//
//        @Autowired
//        private KafkaOrderProducer kafkaProducer;
//
//        @Autowired
//        private KafkaOrderConsumer consumer;
//
//        @Value("${kafka.stream.topic.order}")
//        private String topic;
//
//        @Test
//        public void givenKafkaOrderTopic_whenSendingToTopic_thenMessageReceivedCorrectly() throws Exception {
//
//                IntStream.range(0, 1)
//                                .boxed()
//                                .map(String::valueOf)
//                                .map(s -> OrderAvro
//                                                .newBuilder()
//                                                .setOrderId("order-" + s)
//                                                .setProductId("product - " + s)
//                                                .setCustomerId("customer - " + s)
//                                                .setStatus(Status.NEW)
//                                                .setQuantity(1)
//                                                .setPrice(RAND.nextInt(100))
//                                                .setCurrency(Currency.dlr)
//                                                .build())
//                                        .forEach(orderDTO -> kafkaProducer.send(topic, orderDTO.getOrderId(),
//                                                orderDTO));
//
//                consumer.getLatch().await(6, TimeUnit.SECONDS);
//
//                assertEquals(0, consumer.getLatch().getCount());
//        }

}