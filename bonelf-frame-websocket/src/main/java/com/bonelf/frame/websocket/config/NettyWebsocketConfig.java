package com.bonelf.frame.websocket.config;

import com.bonelf.frame.websocket.netty.NettyWebsocketServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "bonelf.websocket", value = "type", havingValue = "netty")
@Configuration
public class NettyWebsocketConfig {
	@Bean
	public NettyWebsocketServer nettyWebsocketServer() {
		return NettyWebsocketServer.getInstance();
	}
}
