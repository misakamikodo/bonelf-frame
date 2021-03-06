package com.bonelf.support.websocket.norm;

import com.bonelf.frame.websocket.config.NormWebSocketConfig;
import com.bonelf.frame.websocket.property.WebsocketProperties;
import com.bonelf.support.websocket.factory.BnfWsMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebsocketMap初始化存放地址
 **/
@Component
@ConditionalOnBean(NormWebSocketConfig.class)
@Slf4j
@Data
public class NormWebsocketMap implements BnfWsMap {
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
