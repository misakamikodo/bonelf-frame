package com.bonelf.frame.cloud.config;

import com.bonelf.frame.cloud.interceptor.FeignInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web服务配置
 * //@Autowired
 * //private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;
 **/
@Configuration
public class CloudWebMvcConfig implements WebMvcConfigurer {
	/**
	 * 拦截器配置
	 * @param registry
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// feign请求拦截器 规定请求必须通过网关访问
		registry.addInterceptor(new FeignInterceptor())
				.addPathPatterns("/**")
				.excludePathPatterns("/swagger-ui");
	}
}
