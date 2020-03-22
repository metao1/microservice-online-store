package com.metao.product.gateway.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
@Builder
public class ThrottleGatewayFilter implements GatewayFilter {

    private int capacity;

    private int refillTokens;

    private int refillPeriod;

    private TimeUnit refillUnit;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        TokenBucket tokenBucket = TokenBuckets.builder()
                .withCapacity(capacity)
                .withFixedIntervalRefillStrategy(refillTokens, refillPeriod, refillUnit)
                .build();

        boolean consumed = tokenBucket.tryConsume();
        if (consumed) {
            return chain.filter(exchange);
        }
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

}
