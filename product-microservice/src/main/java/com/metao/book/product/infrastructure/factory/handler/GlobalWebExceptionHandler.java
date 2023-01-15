package com.metao.book.product.infrastructure.factory.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@Order(-2)
@RequiredArgsConstructor
public class GlobalWebExceptionHandler implements ErrorWebExceptionHandler {

    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ex.printStackTrace();
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        return badRequest(bufferFactory, exchange, new RuntimeException(ex.getMessage()));
    }

    private Mono<Void> badRequest(
        final DataBufferFactory bufferFactory, ServerWebExchange serverWebExchange,
        Throwable exception
    ) {
        serverWebExchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
        serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return serverWebExchange.getResponse().setComplete();
    }

}