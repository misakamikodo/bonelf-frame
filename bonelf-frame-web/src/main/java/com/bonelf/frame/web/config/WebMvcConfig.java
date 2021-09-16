package com.bonelf.frame.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * web服务配置
 * @author bonelf
 */
@ConditionalOnMissingBean(AbstractWebMvcConfig.class)
@Configuration
public class WebMvcConfig extends AbstractWebMvcConfig {
}
