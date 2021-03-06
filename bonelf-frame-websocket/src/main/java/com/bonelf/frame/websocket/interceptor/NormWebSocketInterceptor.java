package com.bonelf.frame.websocket.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 握手时拦截websocket 中token信息,保证安全险
 **/
@Slf4j
public class NormWebSocketInterceptor implements HandshakeInterceptor {
	@Override
	public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse,
								   WebSocketHandler webSocketHandler, Map<String, Object> map) {
		log.info("---webSocket握手请求---");
		// 前端无法实现 在head放token
		// if (serverHttpRequest instanceof ServletServerHttpRequest) {
		// 	ServletServerHttpRequest servletRequest = (ServletServerHttpRequest)serverHttpRequest;
		// 	// 得到token
		// 	String token = servletRequest.getServletRequest().getHeader(AuthConstant.WEBSOCKET_HEADER);
		// 	if (true) {
		// 	//if (StringUtils.hasText(token)) {
		// 		HttpServletResponse httpServletResponse = ((ServletServerHttpResponse)serverHttpResponse).getServletResponse();
		// 		httpServletResponse.addHeader(AuthConstant.WEBSOCKET_HEADER, token);
		// 		log.info("come in hand Shake ");
		// 		//Long userId = TokenParseUtil.parseJwt(token);
		// 		// if(ToolUtil.isNotEmpty(userId)){
		// 		//     // 通过
		// 		//     return true;
		// 		// }
		// 		return true;
		// 	}
		// 	log.error("token error  ");
		// 	return false;
		// }
		// // 拦截该连接
		// return false;
		//
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse,
							   WebSocketHandler webSocketHandler, Exception e) {
		log.info("webSocket握手结束...");
	}
}

