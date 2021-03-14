package com.bonelf.support.websocket.norm;

import com.bonelf.frame.websocket.property.WebsocketProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebsocketMap初始化存放地址
 **/
@Component
@Slf4j
@Data
public class NormWebsocketMap {
	@Autowired
	private WebsocketProperties websocketProperties;
	/**
	 * userIdStr：session
	 */
	private ConcurrentHashMap<String, WebSocketSession> socketSessionMap;

	@PostConstruct
	public void init() {
		log.info("创建websocket 存储对象");
		/*
		 * 根据预估用户量调整这个初始值大小，避免频繁rehash
		 */
		this.socketSessionMap = new ConcurrentHashMap<>(websocketProperties.getInitSessionMapSize());
	}
}
