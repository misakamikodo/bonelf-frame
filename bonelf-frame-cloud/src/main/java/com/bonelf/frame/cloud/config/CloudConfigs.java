package com.bonelf.frame.cloud.config;

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
@ComponentScan(basePackages = {"com.bonelf.frame.cloud", "com.bonelf.**.feign"})
@Configuration
// @EnableCircuitBreaker
// @EnableDiscoveryClient
public class CloudConfigs {


	/**
	 * <p>
	 * 使用restTemplate进行服务间调用
	 * (@LoadBalanced) 使用了这个就需要使用服务名代替地址
	 * </p>
	 */
	@Bean
	@LoadBalanced
	public RestTemplate registerTemplate() {
		RestTemplate restTemplate = new RestTemplate(getFactory());
		// 这个地方需要配置消息转换器，不然收到消息后转换会出现异常
		restTemplate.setMessageConverters(getConverters());
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

	private List<HttpMessageConverter<?>> getConverters() {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		// String转换器
		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
		List<MediaType> stringMediaTypes = new ArrayList<>();
		// 添加响应数据格式，不匹配会报401
		stringMediaTypes.add(MediaType.TEXT_PLAIN);
		stringMediaTypes.add(MediaType.TEXT_HTML);
		stringConverter.setSupportedMediaTypes(stringMediaTypes);
		messageConverters.add(stringConverter);
		// json
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		List<MediaType> jsonMediaTypes = new ArrayList<>();
		jsonMediaTypes.add(MediaType.APPLICATION_JSON);
		jsonConverter.setSupportedMediaTypes(jsonMediaTypes);
		messageConverters.add(jsonConverter);
		// form
		FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
		List<MediaType> formMediaTypes = new ArrayList<>();
		formMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
		formConverter.setSupportedMediaTypes(formMediaTypes);
		messageConverters.add(formConverter);
		return messageConverters;
	}
}
