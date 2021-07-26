package com.bonelf.support.websocket.factory;

import lombok.extern.slf4j.Slf4j;

/**
 * WebsocketMap
 * @author bonelf
 * @date 2021/5/28 10:42
 */
@Slf4j
public class BnfWsMapFactory {
	public static <T extends BnfWsMap> BnfWsMap create(Class<T> c) {
		BnfWsMap handler = null;
		try {
			handler = (BnfWsMap)Class.forName(c.getName()).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error("创建WebsocketMap失败", e);
		}
		return handler;
	}
}
