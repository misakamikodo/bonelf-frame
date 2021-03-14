package com.bonelf.frame.websocket.property.enums;

/**
 * 缓存类型
 * @author bonelf
 */
public enum WebsocketType {
	/**
	 * 不配置
	 */
	none,
	/**
	 * 默认 stomp
	 */
	stomp,
	/**
	 * netty 需要额外引用netty包
	 */
	netty,
	/**
	 * Spring 通用方式
	 */
	norm
}
