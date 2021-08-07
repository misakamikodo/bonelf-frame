/*
 * Copyright (c) 2021. Bonelf.
 */

package com.bonelf.support.websocket.service.impl;

import cn.hutool.json.JSONObject;
import com.bonelf.cicada.util.EnumUtil;
import com.bonelf.frame.base.util.JsonUtil;
import com.bonelf.frame.core.websocket.SocketMessage;
import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.frame.core.websocket.constant.MessageRecvCmdEnum;
import com.bonelf.frame.websocket.property.WebsocketProperties;
import com.bonelf.frame.websocket.property.enums.TopicType;
import com.bonelf.frame.websocket.property.enums.WebsocketType;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.support.websocket.netty.NettyWebsocketMap;
import com.bonelf.support.websocket.norm.NormWebsocketMap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 使用redis订阅来接收消息并发布，SpringBootWebsocket写法
 * 客户端消息接收(redis)、发送处理
 **/
@Component
@Slf4j
public class SocketMessageService implements MessageListener {
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;
	@Autowired(required = false)
	private NormWebsocketMap normWebsocketMap;
	@Autowired(required = false)
	private NettyWebsocketMap nettyWebsocketMap;
	@Autowired(required = false)
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private WebsocketProperties websocketProperties;

	/**
	 * 作为redis发布订阅接受消息
	 * 网上都是传递String 但是我发现如果传递对象接受也能使用JSON.parseObject解析，并且toString方法和在redis存对象时内容一样
	 * 所以我试着使用redis里的deserialize方法解析body 的 byte。非常成功。
	 * 如果使用了FastJson 注意开启 ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
	 * MessageListenerAdapter 的实现是使用的RedisSerializer的deserialize body数据 可以尝试
	 * {@link org.springframework.data.redis.listener.adapter.MessageListenerAdapter extractMessage(Message)}
	 * @param respMsg class:DefaultMessage body 传递的数据
	 * @param bytes   pattern（convertedChannel）可以使用stringSerializer.deserialize 查看内容
	 */
	@Override
	public void onMessage(Message respMsg, @Nullable byte[] bytes) {
		//SocketRespMessage message = JSON.parseObject(respMsg.toString(), SocketRespMessage.class); //此代码也可反序列化成功
		SocketRespMessage message = (SocketRespMessage)redisTemplate.getValueSerializer().deserialize(respMsg.getBody());
		log.info("接收到消息 {}", message);
		if (message == null) {
			log.warn("can't deserialize message or deserialize fail:{}", respMsg.toString());
			return;
		}
		SocketMessage<?> socketMessage = message.getSocketMessage();
		MessageRecvCmdEnum cmdEnum = EnumUtil.getByCode(socketMessage.getCmdId(), MessageRecvCmdEnum.class);
		JSONObject data = (JSONObject)socketMessage.getData();
		switch (cmdEnum) {
			case PING_PONG:
				SocketRespMessage socketRespMessage = SocketRespMessage.builder()
						.fromUid(message.getFromUid())
						.socketMessage(socketMessage.buildMsg())
						.build();
				if (websocketProperties.getTopicType() == TopicType.feign) {
					// supportFeignClient.sendMessage(socketRespMessage);
				}
				break;
			case TEST:
				// TestDTO testDto = SocketUtil.parseSocketData(data, TestDTO.class);
				// log.debug("Parse Data:" + JsonUtil.toJson(testDto));
				break;
			default:
				//pass
		}
		// Temp Code For restore
		//String[] userIds = socketMessage.getUserIds().split(StrUtil.COMMA);
		//for (String userId : userIds) {
		//	SocketRespMessage socketRespMessage = SocketRespMessage.builder()
		//			.fromUid(userId)
		//			.socketMessage(socketMessage).build();
		//	supportFeignClient.sendMessage(socketRespMessage);
		//}
	}

	/**
	 * 发送消息
	 * @param userId  对象
	 * @param message
	 */
	public void sendMessage(String userId, SocketMessage<?> message) {
		sendMessage(userId, message.getCmdId(), JsonUtil.toJson(message));
	}

	/**
	 * 发送消息
	 * @param userIds 对象
	 * @param message
	 */
	public void sendMessage(Collection<String> userIds, SocketMessage<?> message) {
		for (String userId : userIds) {
			sendMessage(userId, message.getCmdId(), JsonUtil.toJson(message));
		}
	}

	/**
	 * 对所有人发送消息
	 * @param message
	 */
	public void sendAllMessage(SocketMessage<?> message) {
		if (normWebsocketMap != null && websocketProperties.getType() == WebsocketType.norm) {
			normWebsocketMap.getSocketSessionMap().forEach((key, value) -> {
				try {
					value.sendMessage(new TextMessage(JsonUtil.toJson(message)));
				} catch (IOException e) {
					log.error("消息发送失败");
					e.printStackTrace();
				}
			});
		} else if (nettyWebsocketMap != null && websocketProperties.getType() == WebsocketType.netty) {
			nettyWebsocketMap.getSocketSessionMap().forEach((key, value) -> {
				value.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(message)));
			});
		} else if (messagingTemplate != null && websocketProperties.getType() == WebsocketType.stomp) {
			messagingTemplate.convertAndSend("/topic/cmd/" + message.getCmdId(), message);
		}
	}

	/**
	 * 发送消息
	 * @param userId 用户ID
	 * @param cmdId 指令ID
	 * @param message 字符串消息
	 */
	private void sendMessage(String userId, Integer cmdId, String message) {
		if (normWebsocketMap != null && websocketProperties.getType() == WebsocketType.norm) {
			WebSocketSession session = normWebsocketMap.getSocketSessionMap().get(userId);
			if (session != null && session.isOpen()) {
				try {
					session.sendMessage(new TextMessage(message));
				} catch (IOException e) {
					log.error("消息发送失败");
					e.printStackTrace();
				}
			} else {
				cacheUserMsg(userId, message);
			}
		} else if (nettyWebsocketMap != null && websocketProperties.getType() == WebsocketType.netty) {
			Channel ctx = nettyWebsocketMap.getSocketSessionMap().get(userId);
			if (ctx != null && ctx.isOpen()) {
				ctx.writeAndFlush(new TextWebSocketFrame(message));
			} else {
				cacheUserMsg(userId, message);
			}
		} else if (websocketProperties.getType() == WebsocketType.stomp) {
			messagingTemplate.convertAndSend("/topic/user/" + userId + "/" + cmdId, message);
		}
	}

	/**
	 * 缓存用户消息
	 * @param userId
	 * @param message
	 */
	private void cacheUserMsg(String userId, String message) {
		redisTemplate.opsForList().leftPush(String.format(CacheConstant.SOCKET_MSG, userId), message);
		redisTemplate.expire(String.format(CacheConstant.SOCKET_MSG, userId), CacheConstant.SOCKET_MSG_TIME, TimeUnit.DAYS);
	}
}