package com.bonelf.support.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auth 配置
 * @author bonelf
 * @date 2021/8/6 0:02
 */
@Configuration
@Import({
		FtpConfig.class,
		KaptchaConfig.class,
		MinioConfig.class,
		RedisSubscriptionConfig.class,
})
@ComponentScan({
		"com.bonelf.support.property",
		"com.bonelf.support.web",
		"com.bonelf.support.websocket",
})
public class SupportConfig {
}
