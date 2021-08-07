package com.bonelf.frame.cloud.config;

import com.bonelf.frame.base.config.rest.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return RestTemplateBuilder.build();
	}
}
