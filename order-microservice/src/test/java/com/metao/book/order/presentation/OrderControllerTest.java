package com.metao.book.order.presentation;

import com.metao.book.order.application.config.KafkaConfig;
import com.metao.book.order.application.service.OrderMapper;
import com.metao.book.order.application.service.OrderService;
import com.metao.book.order.domain.OrderManageService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import kafka.controller.KafkaController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest(controllers = OrderController.class)
@SpringBootTest
@EmbeddedKafka
public class OrderControllerTest {

        static final String ORDER_URL = "/order/";

        // @Autowired
        // MockMvc mockMvc;

        @Test
        void testCreateOrder_isSuccessful() throws Exception {
                // mockMvc.perform(get(ORDER_URL + "all"))
                //                 .andExpect(status().isOk());
        }
}
