package com.metao.book.product.application.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {

        var httpClient = HttpClient.create()
                .doOnConnected(conn -> conn
                        .addHandlerFirst(new ReadTimeoutHandler(20, TimeUnit.SECONDS))
                        .addHandlerLast(new ReadTimeoutHandler(20, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(20, TimeUnit.SECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("content-type", MediaType.APPLICATION_JSON.toString())
                .defaultHeader("accept", MediaType.APPLICATION_JSON.toString())
                .defaultHeader("user-agent", "agent/2.0")
                .build();

    }

}
