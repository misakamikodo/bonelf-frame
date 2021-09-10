package com.bonelf.support.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 工作流
 * @author bonelf
 * @date 2021/9/2 10:09
 */
@Import({
		FlowableEngineConfig.class
})
@Configuration
public class FlowableAutoConfig{
}
