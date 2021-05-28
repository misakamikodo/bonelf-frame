/*
 * Copyright (c) 2021. Bonelf.
 */

package com.bonelf.support.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * mq启用后 打开@StreamListener注释
 */
@Slf4j
@Component
public class ExampleMqConsumer {

	/**
	 * 可以定义Sink筛选Tag
	 * 也可以使用condition筛选
	 * 我使用Tag筛选Topic，condition筛选head
	 * @param message
	 */
	// @StreamListener(value = ExampleSink.INPUT, condition = "headers['rocketmq_TAGS']=='TestTag'")
	// public void receiveInput(String message) {
	// 	log.info("Receive input: " + message);
	// }
}
