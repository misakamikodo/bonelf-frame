package com.bonelf.frame.websocket.property.enums;

/**
 * websocket渠道分发方式
 * @author bonelf
 */
public enum TopicType {
	/**
	 * 普通或者netty配置下默认，直接在handler处理 ;
	 * stomp类型websocket下默认 使用@MessageMapping接收
	 * 在服务中配置 RedisSubscriptionConfig 注入RedisMessageListenerContainer MessageListenerAdapter
	 * 在SocketMessageService#onMessage(Message, byte[])接收
	 */
	norm,
	/**
	 * redis发布订阅 多项目未使用mq下并使用了通过redis的同一个库可使用
	 * 在服务中配置 RedisSubscriptionConfig 注入RedisMessageListenerContainer MessageListenerAdapter
	 * 在SocketMessageService#onMessage(Message, byte[])接收
	 */
	redis,
	/**
	 * feign 使用@RequestMapping接收 建议使用mq
	 */
	feign,
	/**
	 * mq 使用@StreamListener接收 微服务建议使用
	 */
	mq
}
