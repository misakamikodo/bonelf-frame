package com.bonelf.support.websocket.norm;

import cn.hutool.json.JSONException;
import com.bonelf.frame.core.constant.AuthConstant;
import com.bonelf.frame.core.websocket.SocketMessage;
import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.frame.core.websocket.constant.MessageSendCmdEnum;
import com.bonelf.frame.core.websocket.constant.OnlineStatusEnum;
import com.bonelf.frame.base.util.JsonUtil;
import com.bonelf.frame.base.util.redis.RedisUtil;
// import com.bonelf.frame.mq.bus.MqProducerService;
import com.bonelf.frame.websocket.property.WebsocketProperties;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.support.websocket.MessageRecvCmdEnum;
import com.bonelf.support.websocket.ServiceMsgHandler;
import com.bonelf.support.websocket.SocketMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.*;

import javax.annotation.PostConstruct;
import javax.websocket.PongMessage;
import java.util.List;

/**
 * 这是实现spring websocket 方式之一；
 * 有使用注解@ServletEndPointer 更简单；
 * 不过网关（所以没有shiro鉴权，需要在连接时判断），直接连接；
 * 可通过nginx配置转发
 **/
@Slf4j
@Component
public class NormWebSocketHandler implements WebSocketHandler {
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private NormWebsocketMap normWebsocketMap;
	@Autowired
	private SocketMessageService socketMessageService;
	@Autowired
	private ServiceMsgHandler serviceMsgHandler;
	@Autowired
	private WebsocketProperties websocketProperties;

	@PostConstruct
	public void init() {
		log.info("websocket 加载");
		log.info("属性配置:\n" + JsonUtil.toJson(websocketProperties));
	}

	/**
	 * 建立 保存一份用户信息到redis ,存储session到map里
	 * @param session
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		log.info("建立连接 ");
		List<String> token = session.getHandshakeHeaders().get(AuthConstant.WEBSOCKET_HEADER);
		log.debug("header:" + JsonUtil.toJson(token));
		// FIXME: 2020/10/14 测试用户编号
		String userId = "1";
		if (StringUtils.hasText(userId)) {
			boolean flag = redisUtil.hset(CacheConstant.WEB_SOCKET_SESSION_HASH, userId, OnlineStatusEnum.ONLINE.getCode());
			SocketMessage<?> message = SocketMessage.builder().userIds(userId).cmdId(MessageRecvCmdEnum.INIT_CALLBACK.getCode()).build();
			if (flag) {
				message.setMessage("连接服务器成功");
				normWebsocketMap.getSocketSessionMap().put(userId, session);
				this.sendMsg2AllChannel(userId, message);
			} else {
				message.setMessage("连接服务器失败");
				this.sendMsg2AllChannel(userId, message);
			}
		}
	}

	/**
	 * 给所有定义接入websocket的发送消息
	 * @param userId
	 * @param message
	 */
	private void sendMsg2AllChannel(String userId, SocketMessage<?> message) {
		for (String topicName : websocketProperties.getChannels()) {
			SocketRespMessage msg = SocketRespMessage.builder()
					.fromUid(userId)
					.socketMessage(message)
					.build();
			serviceMsgHandler.sendMessage2Service(msg, topicName);
		}
	}

	/**
	 * 收到消息时发布消息到redis
	 * @param webSocketSession
	 * @param webSocketMessage
	 */
	@Override
	public void handleMessage(WebSocketSession webSocketSession, @NonNull WebSocketMessage<?> webSocketMessage) {
		List<String> token = webSocketSession.getHandshakeHeaders().get(AuthConstant.WEBSOCKET_HEADER);
		log.debug("header:" + JsonUtil.toJson(token));
		// FIXME: 2020/10/15
		String userId = "1";
		if (webSocketMessage instanceof TextMessage) {
			handleTextMessage(userId, (TextMessage)webSocketMessage);
		} else if (webSocketMessage instanceof BinaryMessage) {
			handleBinaryMessage(userId, (BinaryMessage)webSocketMessage);
		} else if (webSocketMessage instanceof PongMessage) {
			//心跳检测
			handlePongMessage(userId, (PongMessage)webSocketMessage);
		} else if (webSocketMessage instanceof PingMessage) {
			//心跳检测
			handlePingMessage(userId, (PingMessage)webSocketMessage);
		}
	}

	/**
	 * 不在这处理消息 见下面配置
	 * 可以根据cmdId 分配不同channel
	 * @param userId
	 * @param webSocketMessage
	 * @see com.bonelf.support.config.RedisSubscriptionConfig
	 * @see SocketMessageService
	 */
	private void handleTextMessage(String userId, TextMessage webSocketMessage) {
		if (webSocketMessage.getPayloadLength() <= 0) {
			return;
		}
		//转SocketMessageHelper
		SocketRespMessage respMessage = new SocketRespMessage();
		SocketMessage<?> socketMessage;
		try {
			socketMessage = JsonUtil.parse(webSocketMessage.getPayload(), SocketMessage.class);
		} catch (JSONException e) {
			log.warn("收到错误的websocket消息，message：{}", webSocketMessage.getPayload());
			SocketMessage<String> errMsg = new SocketMessage<>();
			errMsg.setCmdId(MessageSendCmdEnum.ERR_MSG.getCode());
			errMsg.setMessage("invalid msg");
			errMsg.setData(webSocketMessage.getPayload());
			socketMessageService.sendMessage(userId, errMsg.buildMsg());
			return;
		}
		respMessage.setSocketMessage(socketMessage);
		respMessage.setFromUid(userId);
		for (String topicName : websocketProperties.getCmdChannels().get(String.valueOf(socketMessage.getCmdId()))) {
			serviceMsgHandler.sendMessage2Service(respMessage, topicName);
		}
	}

	private void handlePingMessage(String userId, PingMessage webSocketMessage) {

	}

	private void handlePongMessage(String userId, PongMessage webSocketMessage) {

	}

	private void handleBinaryMessage(String userId, BinaryMessage webSocketMessage) {

	}

	/**
	 * 出错时
	 * @param session
	 * @param throwable
	 * @throws Exception
	 */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable throwable) throws Exception {
		List<String> token = session.getHandshakeHeaders().get(AuthConstant.WEBSOCKET_HEADER);
		log.debug("header:" + JsonUtil.toJson(token));
		// FIXME: 2020/10/14 测试用户编号
		String userId = "1";
		if (StringUtils.hasText(userId)) {
			redisUtil.hset(CacheConstant.WEB_SOCKET_SESSION_HASH, userId, OnlineStatusEnum.OFFLINE.getCode());
			normWebsocketMap.getSocketSessionMap().remove(userId);
		}
		if (session.isOpen()) {
			session.close();
		}
	}

	/**
	 * 关闭
	 * @param session
	 * @param status
	 * @throws Exception
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		//String userId = session.getUri().toString().split("userId=")[1];
		List<String> token = session.getHandshakeHeaders().get(AuthConstant.WEBSOCKET_HEADER);
		log.debug("header:" + JsonUtil.toJson(token));
		// FIXME: 2020/10/14 测试用户编号
		String userId = "1";
		if (StringUtils.hasText(userId)) {
			redisUtil.hset(CacheConstant.WEB_SOCKET_SESSION_HASH, userId, OnlineStatusEnum.OFFLINE.getCode());
			normWebsocketMap.getSocketSessionMap().remove(userId);
		}
		session.close(status);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

}