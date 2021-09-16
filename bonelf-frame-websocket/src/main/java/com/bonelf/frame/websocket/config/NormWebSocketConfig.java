package com.bonelf.frame.websocket.config;

import com.bonelf.frame.websocket.interceptor.NormWebSocketInterceptor;
import com.bonelf.frame.websocket.property.NormWebsocketProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * 配置类 ,采用redis发布订阅功能区做websocket消息的不通服务之间的传递
 **/
@ConditionalOnProperty(prefix = "bonelf.websocket", value = "type", havingValue = "norm")
@ConditionalOnBean(WebSocketHandler.class)
@Configuration
@EnableWebSocket
public class NormWebSocketConfig implements WebSocketConfigurer {
	@Autowired
	private WebSocketHandler webSocketHandler;
	@Autowired
	private NormWebsocketProperties normWebsocketProperties;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		///{userId}
		registry.addHandler(webSocketHandler, normWebsocketProperties.getEndpoint())
				.setAllowedOrigins("*").addInterceptors(webSocketInterceptor());
	}

	public HandshakeInterceptor webSocketInterceptor() {
		return new NormWebSocketInterceptor();
	}

	//@Bean
	//public WebSocketHandler zeusWebSocketHandler() {
	//	return new MainWebSocketHandler();
	//}

	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler threadPoolScheduler = new ThreadPoolTaskScheduler();
		threadPoolScheduler.setThreadNamePrefix("SockJS-");
		threadPoolScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
		threadPoolScheduler.setRemoveOnCancelPolicy(true);
		return threadPoolScheduler;
	}
}