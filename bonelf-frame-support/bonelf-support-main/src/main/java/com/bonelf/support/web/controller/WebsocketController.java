package com.bonelf.support.web.controller;

import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.support.websocket.SocketMessageService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RestController
@RequestMapping("/websocket")
public class WebsocketController {

	@Autowired
	private SocketMessageService socketMessageService;

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
}
