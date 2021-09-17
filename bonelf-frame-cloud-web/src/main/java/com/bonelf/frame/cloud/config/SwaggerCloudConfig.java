package com.bonelf.frame.cloud.config;

import com.bonelf.frame.cloud.swagger.SwaggerCloudPathProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger 微服务配置
 * @author ccy
 * @date 2021/9/17 9:56
 */
@Configuration
public class SwaggerCloudConfig implements InitializingBean {
	@Autowired
	private SwaggerCloudPathProvider pathProvider;
	@Autowired
	private Docket swaggerDocket;

	@Override
	public void afterPropertiesSet() {
		if (swaggerDocket != null) {
			swaggerDocket.pathProvider(pathProvider);
		}
	}
}
