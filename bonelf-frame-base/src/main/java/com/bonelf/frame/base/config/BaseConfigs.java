package com.bonelf.frame.base.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * web服务相关bean注入
 */
@ComponentScan(basePackages = {"com.bonelf.frame.base.property", "com.bonelf.frame.base.util"})
@Import({WechatMiniConfig.class, EhcacheAutoConfig.class, RedisAutoConfig.class})
@Configuration
public class BaseConfigs {
}
