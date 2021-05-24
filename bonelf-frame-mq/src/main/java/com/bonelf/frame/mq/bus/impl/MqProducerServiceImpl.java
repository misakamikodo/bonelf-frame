/*
 * Copyright (c) 2021. Bonelf.
 */

package com.bonelf.frame.mq.bus.impl;

import com.bonelf.frame.base.util.JsonUtil;
import com.bonelf.frame.mq.bus.MqProducerService;
import com.bonelf.frame.mq.property.RocketmqProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * <p>
 * 消息发送
 * </p>
 * @author Chenyuan
 * @since 2021/1/3 18:00
 */
@Slf4j
@Service
public class MqProducerServiceImpl implements MqProducerService {
	private MessageChannel output;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private RocketmqProperties rocketmqProperties;

	@PostConstruct
	public void initChannel() {
		if (rocketmqProperties.getEnable()) {
			try {
				this.output = applicationContext.getBean("output", MessageChannel.class);
			} catch (BeansException e) {
				log.warn("no bean of output is registered!");
			}
		}
	}


	private <T> boolean init(String topic, String tag, T message) {
		if (output == null) {
			log.debug("MQ 服务已经关闭，此条信息topic:{}, tag:{}, message:{}未发送", topic, tag, JsonUtil.toJson(message));
			return false;
		}
		return true;
	}

	@Override
	public <T> boolean send(String topic, String tag, T message) {
		if (!init(topic, tag, message)) {
			return false;
		}
		output.send(MessageBuilder.withPayload(message)
				.setHeader(RocketMQHeaders.TAGS, tag)
				.setHeader(RocketMQHeaders.TOPIC, topic)
				.build());
		return true;
	}

	@Override
	public <T> boolean send(String tag, T message) {
		output.send(MessageBuilder.withPayload(message)
				.setHeader(RocketMQHeaders.TAGS, tag)
				.build());
		return true;
	}

	// @Override
	// public <T> boolean send(ChannelEnum channel, String tag, T message) {
	// 	return send(channel.getTopicName(), tag, message);
	// }
}