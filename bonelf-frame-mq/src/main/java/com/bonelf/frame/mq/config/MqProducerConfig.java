package com.bonelf.frame.mq.config;

import com.bonelf.frame.mq.property.RocketmqProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author bonelf
 * @date 2020/4/21 10:28
 */
@Slf4j
@Getter
@Setter
@ToString
// @Configuration
public class MqProducerConfig {
	@Autowired
	private RocketmqProperties rocketmqProperties = new RocketmqProperties();


	// @Bean
	// @StreamMessageConverter
	// public MappingFastJsonMessageConverter mappingFastJsonMessageConverter() {
	// 	return new MappingFastJsonMessageConverter();
	// }
}
