package com.bonelf.support.websocket.norm;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONException;
import com.bonelf.frame.base.util.JsonUtil;
import com.bonelf.frame.base.util.redis.RedisUtil;
import com.bonelf.frame.core.constant.AuthConstant;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.frame.core.websocket.SocketMessage;
import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.frame.core.websocket.constant.MessageRecvCmdEnum;
import com.bonelf.frame.core.websocket.constant.MessageSendCmdEnum;
import com.bonelf.frame.core.websocket.constant.OnlineStatusEnum;
import com.bonelf.frame.websocket.config.NormWebSocketConfig;
import com.bonelf.frame.websocket.property.WebsocketProperties;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.support.websocket.ServiceMsgHandler;
import com.bonelf.support.websocket.service.impl.SocketMessageService;
import com.bonelf.support.websocket.factory.BnfWsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.*;

import javax.annotation.PostConstruct;
import javax.websocket.PongMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 这是实现spring websocket 方式之一；
 * 有使用注解@ServletEndPointer 更简单；
 * 不过网关（所以没有shiro鉴权，需要在连接时判断），直接连接；
 * 可通过nginx配置转发
 **/
@Slf4j
@ConditionalOnBean(NormWebSocketConfig.class)
@Component
public class NormWebsocketHandler implements WebSocketHandler, BnfWsHandler {
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
	@Autowired
	private TokenStore tokenStore;

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
		log.info("建立连接");
		// 前端无法实现在head中放token
		// List<String> token = session.getHandshakeHeaders().get(AuthConstant.WEBSOCKET_HEADER);
		String userId = null;
		boolean authFail = false;
		if (session.getUri() != null) {
			try {
				userId = getUserIdFromSession(session);
			} catch (OAuth2Exception e) {
				authFail = true;
				closeSessionForAuthFail(session, SocketMessage.builder()
						.cmdId(MessageSendCmdEnum.TOKEN_ERR.getCode())
						.message(e.getMessage()).build());
			}
			log.debug("userid:" + userId);
		} else {
			closeSessionForAuthFail(session, SocketMessage.builder()
					.cmdId(MessageSendCmdEnum.ERR_MSG.getCode())
					.message("uri incorrect").build());
		}
		// 可选择 queryString携带还是再次请求
		if (!authFail && userId != null) {
			judgeLoginByUserId(session, userId);
		}
	}

	/**
	 * 判断认证请求
	 * @param session
	 * @param userId
	 */
	private void judgeLoginByUserId(WebSocketSession session, String userId) {
		if (!session.isOpen()) {
			return;
		}
		if (StringUtils.hasText(userId)) {
			boolean flag = redisUtil.hset(CacheConstant.WEB_SOCKET_SESSION_HASH, userId, OnlineStatusEnum.ONLINE.getCode());
			SocketMessage<?> message = SocketMessage.builder().userIds(userId).cmdId(MessageRecvCmdEnum.INIT_CALLBACK.getCode()).build();
			if (flag) {
				message.setMessage("连接服务器成功");
				normWebsocketMap.getSocketSessionMap().put(userId, session);
				try {
					// 测试输出
					session.sendMessage(
							new TextMessage(JsonUtil.toJson(SocketMessage.builder()
									.cmdId(MessageSendCmdEnum.GREET.getCode())
									.message("hello").build()))
					);
				} catch (IOException e) {
					e.printStackTrace();
					throw new BonelfException(CommonBizExceptionEnum.SERVER_ERROR);
				}
				sendCacheMsg(session, userId);
			} else {
				message.setMessage("连接服务器失败");
				closeSessionForAuthFail(session, SocketMessage.builder()
						.cmdId(MessageSendCmdEnum.ERR_MSG.getCode())
						.message("status set error").build());
			}
		} else {
			closeSessionForAuthFail(session, SocketMessage.builder()
					.cmdId(MessageSendCmdEnum.TOKEN_ERR.getCode())
					.message("auth fail").build());
		}
	}

	private void closeSessionForAuthFail(WebSocketSession session, SocketMessage<?> message) {
		try {
			//XXX 添加消息类型用于判断
			session.sendMessage(
					new TextMessage(JsonUtil.toJson(message)));
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new BonelfException(CommonBizExceptionEnum.SERVER_ERROR);
		}
	}

	/**
	 * 获取用户ID
	 * XXX 更推荐连接后发送一次消息表明身份，不推荐在QueryString添加参数，如果不对 断开链家
	 * @param session
	 * @return
	 */
	private String getUserIdFromSession(WebSocketSession session) {
		if (session.getUri() == null) {
			return null;
		}
		String userId;
		Map<String, String> params = HttpUtil.decodeParamMap(session.getUri().getRawQuery(), StandardCharsets.UTF_8);
		String token = params.get(AuthConstant.WEBSOCKET_AUTH_PARAM);
		userId = getUserIdFromToken(token);
		return userId;
	}

	private String getUserIdFromToken(String token) {
		String userId;
		OAuth2AccessToken accessToken = tokenStore.readAccessToken(token);
		if (accessToken == null) {
			throw new InvalidTokenException("Invalid access token: " + accessToken);
		} else if (accessToken.isExpired()) {
			tokenStore.removeAccessToken(accessToken);
			throw new InvalidTokenException("Access token expired: " + accessToken);
		}
		userId = String.valueOf(accessToken.getAdditionalInformation().get("user_id"));
		return userId;
	}

	/**
	 * 给所有定义接入websocket的发送消息
	 * @param userId
	 * @param message
	 */
	private void sendMsg2AllChannel(String userId, SocketMessage<?> message) {
		for (String tagValue : websocketProperties.getChannels()) {
			SocketRespMessage msg = SocketRespMessage.builder()
					.fromUid(userId)
					.socketMessage(message)
					.build();
			serviceMsgHandler.sendMessage2Service(msg, tagValue);
		}
	}

	/**
	 * 收到消息时发布消息到redis
	 * @param webSocketSession
	 * @param webSocketMessage
	 */
	@Override
	public void handleMessage(WebSocketSession webSocketSession, @NonNull WebSocketMessage<?> webSocketMessage) {
		// List<String> token = webSocketSession.getHandshakeHeaders().get(AuthConstant.WEBSOCKET_HEADER);
		// log.debug("header:" + JsonUtil.toJson(token));
		String userId = getUserIdFromSession(webSocketSession);
		if (webSocketMessage instanceof TextMessage) {
			handleTextMessage(webSocketSession, userId, (TextMessage)webSocketMessage);
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
	 * @param webSocketSession
	 * @param userId
	 * @param webSocketMessage
	 * @see com.bonelf.support.config.RedisSubscriptionConfig
	 * @see SocketMessageService
	 */
	private void handleTextMessage(WebSocketSession webSocketSession, String userId, TextMessage webSocketMessage) {
		if (webSocketMessage.getPayloadLength() <= 0) {
			return;
		}
		log.warn("收到：{}", webSocketMessage.getPayload());
		//转SocketMessageHelper
		SocketRespMessage respMessage = new SocketRespMessage();
		SocketMessage<?> socketMessage;
		try {
			socketMessage = JsonUtil.parse(webSocketMessage.getPayload(), SocketMessage.class);
		} catch (JSONException e) {
			log.warn("收到错误的websocket消息，message：{}", webSocketMessage.getPayload());
			if (userId != null) {
				SocketMessage<String> errMsg = new SocketMessage<>();
				errMsg.setCmdId(MessageSendCmdEnum.ERR_MSG.getCode());
				errMsg.setMessage("invalid msg");
				errMsg.setData(webSocketMessage.getPayload());
				socketMessageService.sendMessage(userId, errMsg.buildMsg());
			}
			return;
		}
		if (socketMessage != null && MessageRecvCmdEnum.AUTH.getCode().equals(socketMessage.getCmdId())) {
			Map<String, String> map = (Map<String, String>)socketMessage.getData();
			String token = map.get(AuthConstant.WEBSOCKET_AUTH_PARAM);
			String channelUserId = null;
			boolean authFail = false;
			try {
				channelUserId = getUserIdFromToken(token);
			} catch (OAuth2Exception e) {
				closeSessionForAuthFail(webSocketSession, SocketMessage.builder()
						.cmdId(MessageSendCmdEnum.TOKEN_ERR.getCode())
						.message(e.getMessage()).build());
				authFail = true;
			}
			if (!authFail) {
				judgeLoginByUserId(webSocketSession, channelUserId);
			}
		} else {
			if (userId == null) {
				closeSessionForAuthFail(webSocketSession, SocketMessage.builder()
						.cmdId(MessageSendCmdEnum.TOKEN_ERR.getCode())
						.message("Not Auth").build());
			}
			respMessage.setSocketMessage(socketMessage);
			respMessage.setFromUid(userId);
			if (socketMessage != null && socketMessage.getChannel() != null) {
				for (String tagValue : socketMessage.getChannel()) {
					serviceMsgHandler.sendMessage2Service(respMessage, tagValue);
				}
			}
		}
	}

	private void handlePingMessage(String userId, PingMessage webSocketMessage) {

	}

	private void handlePongMessage(String userId, PongMessage webSocketMessage) {

	}

	private void handleBinaryMessage(String userId, BinaryMessage webSocketMessage) {

	}

	/**
	 * 发送缓存消息
	 * @param session
	 * @param channelUserId
	 */
	private void sendCacheMsg(WebSocketSession session, String channelUserId) {
		List<Object> msgs = redisUtil.lGetAll(String.format(CacheConstant.SOCKET_MSG, channelUserId));
		if (msgs != null && msgs.size() > 0) {
			boolean fail = false;
			try {
				session.sendMessage(new TextMessage(JsonUtil.toJson(msgs)));
			} catch (IOException e) {
				fail = true;
				e.printStackTrace();
			}
			if (!fail) {
				// XXX 需要判断保证是否发送成功，因为websocket不是可靠连接，这个数据不能丢失；建议是前端收到后发送一个确认标识后删除未读消息
				// redisUtil.del(String.format(CacheConstant.SOCKET_MSG, channelUserId));
			}
		}
	}

	/**
	 * 出错时
	 * @param session
	 * @param throwable
	 * @throws Exception
	 */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable throwable) throws Exception {
		List<String> token = session.getHandshakeHeaders().get(AuthConstant.WEBSOCKET_AUTH_PARAM);
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
		// List<String> token = session.getHandshakeHeaders().get(AuthConstant.WEBSOCKET_AUTH_PARAM);
		// log.debug("header:" + JsonUtil.toJson(token));
		// 2020/10/14 测试用户编号
		String userId = getUserIdFromSession(session);
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
