package com.metao.product.checkout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CheckoutMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CheckoutMicroserviceApplication.class);
        app.setWebApplicationType(WebApplicationType.REACTIVE);
    }

}
