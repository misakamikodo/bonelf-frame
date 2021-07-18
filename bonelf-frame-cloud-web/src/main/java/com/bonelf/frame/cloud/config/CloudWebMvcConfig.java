package com.bonelf.frame.cloud.config;

import com.bonelf.frame.base.util.SpringContextUtils;
import com.bonelf.frame.cloud.interceptor.FeignInterceptor;
import com.bonelf.frame.web.config.AbstractWebMvcConfig;
import com.bonelf.frame.web.core.interceptor.DebugInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * web服务配置
 * @author bonelf
 **/
@Configuration
public class CloudWebMvcConfig extends AbstractWebMvcConfig {
	/**
	 * 拦截器配置
	 * @param registry
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 接口debug拦截器
		if (!SpringContextUtils.isProdProfile()) {
			registry.addInterceptor(new DebugInterceptor())
					.addPathPatterns("/**");
		}
		// feign请求拦截器 规定请求必须通过网关访问
		registry.addInterceptor(new FeignInterceptor())
				.addPathPatterns("/**")
				.excludePathPatterns("/swagger-ui");
	}
}
