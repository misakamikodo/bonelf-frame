package com.bonelf.frame.websocket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Websocket 测试
 * @author ccy
 * @date 2021/5/26 17:21
 */
@RestController
@RequestMapping("/noAuth/websocket")
public class StompGreetingController {
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	/**
	 * 不需要再加 /websocket/greeting
	 * 接受
	 * @param headerAccessor
	 * @return
	 */
    @MessageMapping("/greeting")
    public String greeting(StompHeaderAccessor headerAccessor) {
        return "hello";
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
	@SendToUser("/topic/greeting")
    public String greeting2() {
        return "hello";
    }
}
