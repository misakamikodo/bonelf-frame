package com.bonelf.frame.gateway.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = {
		"com.bonelf.frame.gateway.controller",
		"com.bonelf.frame.gateway.filter",
		"com.bonelf.frame.gateway.handler",
		"com.bonelf.frame.gateway.service.impl",
})
@Import({
		GatewayRoutersConfig.class,
		SwaggerConfig.class,
})
@Configuration
public class GatewayConfigs {
}
