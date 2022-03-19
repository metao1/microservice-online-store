package com.metao.book.order.presentation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

//@Import()
@WebFluxTest(controllers = OrderController.class)
public class OrderControllerTest {

        private static final String ORDER_URL = "/order/";

        @Autowired
        WebTestClient webTestClient;

        @Test
        void testCreateOrder() {
                webTestClient
                        .get()
                        .uri(ORDER_URL+"all")
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful();
        }
}
