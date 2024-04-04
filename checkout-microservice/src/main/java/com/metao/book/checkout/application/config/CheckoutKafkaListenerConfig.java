package com.metao.book.checkout.application.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Slf4j
@Configuration
@EnableKafka
@RequiredArgsConstructor
public class CheckoutKafkaListenerConfig {
}
