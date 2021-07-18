package com.bonelf.frame.websocket.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = {
		"com.bonelf.frame.websocket.controller",
		"com.bonelf.frame.websocket.event",
		"com.bonelf.frame.websocket.property",
})
@Import({
		NettyWebsocketConfig.class,
		NormWebSocketConfig.class,
		StompWebSocketConfig.class
})
@Configuration
public class WebSocketConfigs {
}
