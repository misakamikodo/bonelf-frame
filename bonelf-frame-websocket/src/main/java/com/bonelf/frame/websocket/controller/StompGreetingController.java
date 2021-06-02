package com.bonelf.frame.websocket.controller;

import cn.hutool.json.JSONUtil;
import com.bonelf.frame.websocket.config.StompWebSocketConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Websocket 测试
 * @author ccy
 * @date 2021/5/26 17:21
 */
@Slf4j
@RestController
@ConditionalOnBean(StompWebSocketConfig.class)
@RequestMapping("/noAuth/websocket")
public class StompGreetingController {
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	/**
	 * 不需要再加 /websocket/greeting
	 * 接受
	 * @param headerAccessor
	 * @param data
	 * @return
	 */
	@MessageMapping("/greeting")
	public void greeting(StompHeaderAccessor headerAccessor, Map<Object, Object> data) {
		log.info("收到消息:\n" + JSONUtil.toJsonStr(data));
	}

	/**
	 * 发送1
	 * @return
	 */
	@PostMapping("/greeting")
	public String greeting() {
		messagingTemplate.convertAndSend("/topic/greeting", "hello");
		return "hello";
	}

	/**
	 * 发送2
	 * @return
	 */
	@PostMapping("/greeting2")
	@SendTo("/topic/greeting")
	public String greeting2() {
		return "hello";
	}
}
