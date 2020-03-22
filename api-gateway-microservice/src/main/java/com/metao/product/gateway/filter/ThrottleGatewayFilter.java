package com.metao.product.gateway.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ThrottleGatewayFilter extends AbstractGatewayFilterFactory<ThrottleGatewayFilter.Config> {

    public ThrottleGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(ThrottleGatewayFilter.Config config) {
        return (exchange, chain) -> {
            TokenBucket tokenBucket = TokenBuckets.builder()
                    .withCapacity(config.getCapacity())
                    .withFixedIntervalRefillStrategy(config.getRefillTokens(),
                            config.getRefillPeriod(), TimeUnit.SECONDS)
                    .build();

            boolean consumed = tokenBucket.tryConsume();
            if (consumed) {
                return chain.filter(exchange);
            }
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        };
    }

    @Setter
    @Getter
    public static class Config {

        private int capacity;

        private int refillTokens;

        private int refillPeriod;

        private TimeUnit refillUnit;

    }
}
