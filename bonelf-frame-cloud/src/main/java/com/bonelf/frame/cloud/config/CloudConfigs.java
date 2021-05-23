package com.bonelf.frame.cloud.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 微服务相关bean注入
 */
@EnableFeignClients(basePackages = {"com.bonelf.**.feign"})
@ComponentScan(basePackages = {"com.bonelf.frame.cloud","com.bonelf.**.feign"})
@Configuration
// @EnableCircuitBreaker
// @EnableDiscoveryClient
public class CloudConfigs {


	/**
	 * <p>
	 * 使用restTemplate进行服务间调用
	 * </p>
	 */
	@Bean
	@LoadBalanced
	public RestTemplate registerTemplate() {
		RestTemplate restTemplate = new RestTemplate(getFactory());
		//这个地方需要配置消息转换器，不然收到消息后转换会出现异常
		restTemplate.setMessageConverters(getConverts());
		return restTemplate;
	}

	private SimpleClientHttpRequestFactory getFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		// 解决401报错时，报java.net.HttpRetryException: cannot retry due to server authentication, in streaming mode
		factory.setOutputStreaming(false);
		// factory.setConnectTimeout(connectionTimeout);
		// factory.setReadTimeout(readTimeout);
		return factory;
	}

	private List<HttpMessageConverter<?>> getConverts() {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		// String转换器
		StringHttpMessageConverter stringConvert = new StringHttpMessageConverter();
		List<MediaType> stringMediaTypes = new ArrayList<MediaType>() {{
			//添加响应数据格式，不匹配会报401
			add(MediaType.TEXT_PLAIN);
			add(MediaType.TEXT_HTML);
			add(MediaType.APPLICATION_JSON);
		}};
		stringConvert.setSupportedMediaTypes(stringMediaTypes);
		messageConverters.add(stringConvert);
		return messageConverters;
	}
}
