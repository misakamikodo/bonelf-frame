package com.bonelf.frame.websocket.config;

import com.bonelf.frame.websocket.interceptor.StompWebsocketInterceptor;
import com.bonelf.frame.websocket.property.StompWebsocketProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * <p>
 * stomp协议的websocket
 * <a href='http://localhost:8080/bonelf/'>Welcome to SockJS!</a>
 * </p>
 * @see com.bonelf.frame.websocket.property.enums.WebsocketType
 * @author bonelf
 * @since 2021/2/14 11:00
 */
@ConditionalOnProperty(prefix = "bonelf.websocket", value = "type", havingValue = "stomp")
@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {
	@Autowired
	private StompWebsocketProperties stompWebsocketProperties;

	/**
	 * 注册stomp端点，主要是起到连接作用
	 * @param stompEndpointRegistry
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		stompEndpointRegistry
				// 端点名称
				.addEndpoint(stompWebsocketProperties.getEndpoint())
				// 握手处理，主要是连接的时候认证获取其他数据验证等
				//.setHandshakeHandler()
				// 拦截处理，和http拦截类似
				.addInterceptors(new StompWebsocketInterceptor())
				.setAllowedOrigins("*") //跨域
				.withSockJS(); //使用sockJS
	}

	/**
	 * 注册相关服务.
	 * @param registry
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 这里使用的是内存模式，生产环境可以使用mq。
		// 这里注册两个，主要是目的是将广播和队列分开。
		// registry.enableStompBrokerRelay().setRelayHost().setRelayPort() 其他方式
		registry.enableSimpleBroker(stompWebsocketProperties.getEndpoint(), stompWebsocketProperties.getQueue());
		// 客户端名称前缀
		registry.setApplicationDestinationPrefixes(stompWebsocketProperties.getAppDestinationPredix());
		// 用户名称前缀
		registry.setUserDestinationPrefix(stompWebsocketProperties.getUserDestinationPredix());
	}
}
