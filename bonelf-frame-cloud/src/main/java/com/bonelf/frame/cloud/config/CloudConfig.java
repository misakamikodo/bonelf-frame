package com.bonelf.frame.cloud.config;

import com.bonelf.frame.base.config.rest.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * 微服务相关bean注入
 */
@EnableFeignClients(basePackages = {"com.bonelf.**.feign"})
@ComponentScan(basePackages = {
		"com.bonelf.**.feign"
})
@Configuration
// @EnableCircuitBreaker
// @EnableDiscoveryClient
public class CloudConfig {


	/**
	 * <p>
	 * 使用restTemplate进行服务间调用
	 * (@LoadBalanced) 使用了这个就需要使用服务名代替地址
	 * </p>
	 */
	@Primary
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return RestTemplateBuilder.build();
	}
}
