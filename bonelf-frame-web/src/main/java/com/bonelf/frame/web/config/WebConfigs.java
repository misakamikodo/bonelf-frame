package com.bonelf.frame.web.config;

import com.bonelf.frame.web.config.security.ResourceServerConfig;
import com.bonelf.frame.web.config.swagger.Swagger2Config;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * web服务扫包配置
 * @author ccy
 * @date 2021/4/29 11:44
 */
@Configuration
@ComponentScan(basePackages = {
		"com.bonelf.frame.web.aop",
		"com.bonelf.frame.web.core.handler",
		"com.bonelf.frame.web.core",
		"com.bonelf.frame.web.security",
		"com.bonelf.frame.web.service.impl",
		"com.bonelf.frame.web.util",
})
@Import({
		ResourceServerConfig.class,
		Swagger2Config.class,
})
public class WebConfigs {
}
