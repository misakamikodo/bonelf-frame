package com.bonelf.frame.gateway.config;

import com.bonelf.frame.gateway.handler.CircuitFallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

/**
 * 路由配置信息
 * @author bonelf
 * @date 13点19分
 */
@Slf4j
@Configuration
public class GatewayRoutersConfig {
	@Autowired
	private CircuitFallbackHandler circuitFallbackHandler;

	@Bean
	public RouterFunction<?> routerFunction() {
		return RouterFunctions.route(
				RequestPredicates.path("/fallback").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), circuitFallbackHandler);

	}

}
