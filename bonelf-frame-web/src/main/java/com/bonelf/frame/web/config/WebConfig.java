package com.bonelf.frame.web.config;

import com.bonelf.frame.base.config.rest.RestTemplateBuilder;
import com.bonelf.frame.web.config.security.ResourceServerConfig;
import com.bonelf.frame.web.config.swagger.Swagger2Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

/**
 * web服务扫包配置
 * @author bonelf
 * @date 2021/4/29 11:44
 */
@Configuration
@ComponentScan(basePackages = {
		"com.bonelf.frame.web.aop",
		"com.bonelf.frame.web.core.handler",
		"com.bonelf.frame.web.core",
		"com.bonelf.frame.web.security",
		"com.bonelf.frame.web.core.dict.service.impl",
		"com.bonelf.frame.web.util",
})
@Import({
		ResourceServerConfig.class,
		Swagger2Config.class,
})
public class WebConfig {
	/**
	 * <p>
	 * 使用restTemplate进行Http调用
	 * </p>
	 */
	@Bean("defaultRestTemplate")
	public RestTemplate restTemplate() {
		return RestTemplateBuilder.build();
	}
}
