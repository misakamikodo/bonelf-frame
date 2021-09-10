package com.bonelf.support.config;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

/**
 * 工作流
 * @author bonelf
 * @date 2021/9/2 10:09
 */
@Configuration
public class FlowableAutoConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
	@Override
	public void configure(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
		springProcessEngineConfiguration.setActivityFontName("宋体");
		springProcessEngineConfiguration.setLabelFontName("宋体");
		springProcessEngineConfiguration.setAnnotationFontName("宋体");
	}
}
