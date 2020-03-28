package com.metao.product.gateway.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SCGWPreFilter extends AbstractGatewayFilterFactory<SCGWPreFilter.Config> {

	public SCGWPreFilter() {
		super(Config.class);
	}
	
	@Override
	public GatewayFilter apply(Config config) {
		log.info("inside SCGWPreFilter.apply method");
		
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest().mutate().header("product-pre-header", Math.random()*10+"").build();
			return chain.filter(exchange.mutate().request(request).build());
		};
	}

	@Setter
	@Getter
	public static class Config {
		private String name;
	}
}