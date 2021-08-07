package com.bonelf.auth.config;

import com.bonelf.auth.config.permission.CustomPermissionEvaluator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auth 配置
 * @author ccy
 * @date 2021/8/6 0:02
 */
@Configuration
@Import({
		CustomPermissionEvaluator.class,
		AuthorizationServerConfig.class,
		WebServerSecurityConfig.class,
})
@ComponentScan({
		"com.bonelf.auth.core",
		"com.bonelf.auth.service.impl",
		"com.bonelf.auth.web",
})
public class AuthConfig {
}
