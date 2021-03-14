
package com.bonelf.frame.websocket.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.NonNullApi;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * <p>
 * websocket拦截器
 * </p>
 * @author Chenyuan
 * @since 2021/2/17 15:30
 */
@Slf4j
public class StompWebsocketInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, @NonNull ServerHttpResponse response,
								   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
		log.info("beforeHandshake");
		return request.getHeaders().getOrigin() != null;
	}

	@Override
	public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
							   @NonNull WebSocketHandler wsHandler, @Nullable Exception exception) {
		log.info("afterHandshake");
	}

}
