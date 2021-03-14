package com.bonelf.frame.base.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * web服务相关bean注入
 */
// @EnableFeignClients(basePackages = {"com.bonelf.support.feign"})
@ComponentScan(basePackages = {"com.bonelf.frame.base"})
@Configuration
public class BaseConfigs {
}
