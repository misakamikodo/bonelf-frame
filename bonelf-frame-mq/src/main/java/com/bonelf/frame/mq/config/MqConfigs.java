package com.bonelf.frame.mq.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = {
		"com.bonelf.frame.mq.bus.impl",
		"com.bonelf.frame.mq.property"
})
@Import({MqProducerConfig.class})
@Configuration
public class MqConfigs {
}
