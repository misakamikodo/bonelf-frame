package com.bonelf.support.websocket.factory;

import lombok.extern.slf4j.Slf4j;

/**
 * WebsocketHandler
 * @author bonelf
 * @date 2021/5/28 10:42
 */
@Slf4j
public class BnfWsHandlerFactory {
	public static <T extends BnfWsHandler> BnfWsHandler create(Class<T> c) {
		BnfWsHandler handler = null;
		try {
			handler = (BnfWsHandler)Class.forName(c.getName()).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error("创建WebsocketHandler失败", e);
		}
		return handler;
	}
}
