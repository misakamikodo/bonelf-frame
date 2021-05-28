/*
 * Copyright (c) 2021. Bonelf.
 */

package com.bonelf.frame.mq.bus;

/**
 * <p>
 *     mq消息发送
 * </p>
 * @author Chenyuan
 * @since 2021/1/3 18:00
 */
public interface MqProducerService {
	/**
	 * 使用Source配置的topic发送
	 * 推荐使用这个
	 * @param tag
	 * @param message
	 * @param <T>
	 * @return
	 */
	<T> boolean send(String tag, T message);

	/**
	 * 发送消息
	 * @param topic 自定义主题
	 * @param tag 消息标签
	 * @param message 消息
	 * @param <T>
	 * @return
	 */
	@Deprecated
	<T> boolean send(String topic, String tag, T message);
}
