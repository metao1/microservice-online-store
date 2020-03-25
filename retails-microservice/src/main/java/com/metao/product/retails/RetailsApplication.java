package com.metao.product.retails;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;

@SpringBootApplication
@EnableEurekaClient
@EnableBinding(Sink.class)
public class RetailsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetailsApplication.class, args);
    }

}
