package com.bonelf.frame.cloud.config;

import com.bonelf.frame.cloud.config.security.CloudResourceServerConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * mvc web 配置
 * @author bonelf
 * @date 2021/4/29 11:45
 */
@Import({
		CloudWebMvcConfig.class,
		CloudResourceServerConfig.class,
})
@ComponentScan(basePackages = {
		"com.bonelf.frame.cloud.service.impl"
})
@Configuration
public class WebCloudConfig {

}
