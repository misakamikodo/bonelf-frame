package com.bonelf.support.web.controller;

import cn.hutool.json.JSONUtil;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.core.websocket.SocketMessage;
import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.support.websocket.ServiceMsgHandler;
import com.bonelf.support.websocket.service.impl.SocketMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * websocket服务请求
 * </p>
 * @author bonelf
 * @since 2020/10/18 20:29
 */
@Slf4j
@RestController
@RequestMapping("/websocket")
public class WebsocketController {

	@Autowired
	private SocketMessageService socketMessageService;
	@Autowired
	private ServiceMsgHandler serviceMsgHandler;

	/**
	 * <p>
	 * 发送消息
	 * </p>
	 * @author bonelf
	 * @since 2020/10/5 21:57
	 */
	@PostMapping("/v1/sendMessage")
	public Result<String> sendMessage(@RequestBody SocketRespMessage message) {
		socketMessageService.sendMessage(message.getFromUid(), message.getSocketMessage());
		return Result.ok();
	}


	/**
	 * stomp 消息接口 处理
	 * @param headerAccessor
	 * @param data
	 */
	@MessageMapping("/{channel}/greeting")
	public void greeting(StompHeaderAccessor headerAccessor, @DestinationVariable String channel, SocketMessage<?> data) {
		log.info("收到消息:\n" + JSONUtil.toJsonStr(data));
		SocketRespMessage message = SocketRespMessage.builder().socketMessage(data).fromUid(headerAccessor.getFirstNativeHeader("userId")).build();
		serviceMsgHandler.sendMessage2Service(message, channel);
	}
}
