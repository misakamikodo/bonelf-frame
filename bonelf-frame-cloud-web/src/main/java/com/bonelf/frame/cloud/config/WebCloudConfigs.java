package com.bonelf.frame.cloud.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * mvc web 配置
 * @author ccy
 * @date 2021/4/29 11:45
 */
@ComponentScan(basePackages = {
		"com.bonelf.frame.cloud.service.impl"
})
@Configuration
public class WebCloudConfigs {

}
