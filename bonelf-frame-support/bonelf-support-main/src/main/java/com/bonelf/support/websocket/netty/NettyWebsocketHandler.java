package com.bonelf.support.websocket.netty;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.bonelf.frame.base.util.JsonUtil;
import com.bonelf.frame.base.util.redis.RedisUtil;
import com.bonelf.frame.core.constant.AuthConstant;
import com.bonelf.frame.core.websocket.SocketMessage;
import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.frame.core.websocket.constant.MessageRecvCmdEnum;
import com.bonelf.frame.core.websocket.constant.MessageSendCmdEnum;
import com.bonelf.frame.core.websocket.constant.OnlineStatusEnum;
import com.bonelf.frame.websocket.config.NettyWebsocketConfig;
import com.bonelf.frame.websocket.property.WebsocketProperties;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.support.websocket.ServiceMsgHandler;
import com.bonelf.support.websocket.service.impl.SocketMessageService;
import com.bonelf.support.websocket.factory.BnfWsHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * websocket
 * -@ChannelHandler.Sharable：线程安全的
 * </p>
 * @author bonelf
 * @since 2020/10/18 18:34
 */
@Slf4j
@ConditionalOnBean(NettyWebsocketConfig.class)
@ChannelHandler.Sharable
@Component
public class NettyWebsocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> implements BnfWsHandler {
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private NettyWebsocketMap nettyWebsocketMap;
	@Autowired
	private SocketMessageService socketMessageService;
	@Autowired
	private ServiceMsgHandler serviceMsgHandler;
	@Autowired
	private WebsocketProperties websocketProperties;
	@Autowired
	private TokenStore tokenStore;

	// @Override
	// public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// 需要重写处理uri附带参数，要不然如果带上链接参数不会允许链接
		// if (msg instanceof FullHttpRequest) {
		// 	FullHttpRequest request = (FullHttpRequest)msg;
		// 	String uri = request.uri();
		// 	String origin = request.headers().get("Origin");
		// 	if (null == origin) {
		// 		ctx.close();
		// 	} else {
		// 		if (null != uri && uri.contains("?")) {
		// 			String[] uriArray = uri.split("\\?");
		// 			if (uriArray.length > 1) {
		// 				String[] paramsArray = uriArray[1].split("=");
		// 				if (paramsArray.length > 1) {
		// 					// 验证连接权限，不通过关闭
		// 				}
		// 			}
		// 			request.setUri(uriArray[0]);
		// 		}
		// 	}
		// }
	// 	super.channelRead(ctx, msg);
	// }

	/**
	 * 使用redis发布订阅收发消息
	 */
	//@Autowired
	//@Deprecated
	//private StringRedisTemplate stringRedisTemplate;
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
		handleWebSocketFrame(ctx, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("exceptionCaught", cause);
		String userId = ctx.channel().attr(NettyWebsocketMap.USER_ID_CHANNEL_KEY).get();
		if (StringUtils.hasText(userId)) {
			redisUtil.hset(CacheConstant.WEB_SOCKET_SESSION_HASH, userId, OnlineStatusEnum.OFFLINE.getCode());
			nettyWebsocketMap.getSocketSessionMap().remove(userId);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("channelInactive");
		String userId = ctx.channel().attr(NettyWebsocketMap.USER_ID_CHANNEL_KEY).get();
		if (StringUtils.hasText(userId)) {
			redisUtil.hset(CacheConstant.WEB_SOCKET_SESSION_HASH, userId, OnlineStatusEnum.OFFLINE.getCode());
			nettyWebsocketMap.getSocketSessionMap().remove(userId);
		}
	}

	/**
	 * handShake 流程
	 * WebSocketServerProtocolHandler.ServerHandshakeStateEvent->WebSocketServerProtocolHandler.HandshakeComplete
	 * @param ctx
	 * @param evt
	 * @throws Exception
	 */
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		log.info("userEventTriggered");
		if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
			this.handleHandleCompleteEvt(ctx, evt);
		}
	}

	/**
	 * 握手成功事件
	 * @param ctx
	 * @param evt
	 */
	private void handleHandleCompleteEvt(ChannelHandlerContext ctx, Object evt) {
		WebSocketServerProtocolHandler.HandshakeComplete httpRequest = (WebSocketServerProtocolHandler.HandshakeComplete)evt;
		log.info("connect event");
		// log.debug("header:" + JSON.toJSONString(token));
		// String token = httpRequest.requestHeaders().get(AuthConstant.WEBSOCKET_AUTH_PARAM);
		// 用户编号
		String userId = null;
		boolean authFail = false;
		try {
			userId = getUserIdFromSession(httpRequest);
		} catch (OAuth2Exception e) {
			closeSessionForAuthFail(ctx, SocketMessage.builderWithTimestamp()
					.cmdId(MessageSendCmdEnum.TOKEN_ERR.getCode())
					.message(e.getMessage()).build());
			authFail = true;
		}
		log.debug("userId:" + userId);
		// 可选择 queryString携带还是再次请求
		if (!authFail) {
			if (userId != null) {
				judgeLoginByUserId(ctx, userId);
			}
			ctx.channel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(SocketMessage.builderWithTimestamp()
					.cmdId(MessageSendCmdEnum.GREET.getCode())
					.message("hello").build())));
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

		if (frame instanceof TextWebSocketFrame) {
			log.debug("收到文本消息:{}", ((TextWebSocketFrame)frame).text());
			handleTextMessage(ctx, ctx.channel().attr(NettyWebsocketMap.USER_ID_CHANNEL_KEY).get(), (TextWebSocketFrame)frame);
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		if (frame instanceof CloseWebSocketFrame) {
			ctx.writeAndFlush(frame.retainedDuplicate()).addListener(ChannelFutureListener.CLOSE);
			return;
		}
		if (frame instanceof BinaryWebSocketFrame) {
			log.info("BinaryWebSocketFrame");
			return;
		}
		if (frame instanceof PongWebSocketFrame) {
			return;
		}
	}

	private void handleTextMessage(ChannelHandlerContext ctx, String userId, TextWebSocketFrame frame) {
		if (!StringUtils.hasText(frame.text())) {
			return;
		}
		//转SocketMessageHelper
		SocketRespMessage respMessage = new SocketRespMessage();
		SocketMessage<?> socketMessage;
		try {
			// 需要根据cmdId确定转发的服务 所以只有在这里解析一遍数据
			socketMessage = JSON.parseObject(frame.text(), SocketMessage.class);
		} catch (JSONException e) {
			log.warn("收到错误的websocket消息，message：{}", frame.text());
			if (userId != null) {
				SocketMessage<String> errMsg = new SocketMessage<>();
				errMsg.setCmdId(MessageSendCmdEnum.ERR_MSG.getCode());
				errMsg.setData(frame.text());
				errMsg.setMessage("invalid message");
				socketMessageService.sendMessage(userId, errMsg.buildMsg());
			}
			return;
		}
		if (socketMessage != null) {
			if (MessageRecvCmdEnum.AUTH.getCode().equals(socketMessage.getCmdId())) {
				Map<String, String> map = (Map<String, String>)socketMessage.getData();
				String token = map.get(AuthConstant.WEBSOCKET_AUTH_PARAM);
				String channelUserId = null;
				boolean authFail = false;
				try {
					channelUserId = getUserIdFromToken(token);
				} catch (OAuth2Exception e) {
					closeSessionForAuthFail(ctx, SocketMessage.builderWithTimestamp()
							.cmdId(MessageSendCmdEnum.TOKEN_ERR.getCode())
							.message(e.getMessage()).build());
					authFail = true;
				}
				if (!authFail) {
					judgeLoginByUserId(ctx, channelUserId);
				}
			} else {
				if (userId == null) {
					closeSessionForAuthFail(ctx, SocketMessage.builderWithTimestamp()
							.cmdId(MessageSendCmdEnum.TOKEN_ERR.getCode())
							.message("Not Auth").build());
				}
				respMessage.setSocketMessage(socketMessage);
				respMessage.setFromUid(userId);
				for (String tagValue : socketMessage.getChannel()) {
					serviceMsgHandler.sendMessage2Service(respMessage, tagValue);
				}
			}
		}
	}

	/**
	 * 判断认证请求
	 * @param ctx
	 * @param channelUserId
	 */
	private void judgeLoginByUserId(ChannelHandlerContext ctx, String channelUserId) {
		if (!ctx.channel().isOpen()) {
			return;
		}
		if (StringUtils.hasText(channelUserId)) {
			boolean flag = redisUtil.hset(CacheConstant.WEB_SOCKET_SESSION_HASH, channelUserId, OnlineStatusEnum.ONLINE.getCode());
			SocketMessage<?> message = SocketMessage.builderWithTimestamp().userIds(channelUserId).cmdId(MessageRecvCmdEnum.INIT_CALLBACK.getCode()).build();
			if (flag) {
				message.setMessage("连接服务器成功");
				ctx.channel().attr(NettyWebsocketMap.USER_ID_CHANNEL_KEY).set(channelUserId);
				nettyWebsocketMap.getSocketSessionMap().put(channelUserId, ctx.channel());
				//websocketMap.getChannelGroup().add(ctx.channel());
				log.info("连接服务器成功");
				ctx.writeAndFlush(new TextWebSocketFrame("hello"));
				sendCacheMsg(ctx, channelUserId);
				// this.sendMsg2AllChannel(userId, message);
			} else {
				message.setMessage("连接服务器失败");
				// this.sendMsg2AllChannel(userId, message);
				log.info("连接服务器失败");
				closeSessionForAuthFail(ctx, SocketMessage.builderWithTimestamp()
						.cmdId(MessageSendCmdEnum.ERR_MSG.getCode())
						.message("status set error").build());
			}
		} else {
			closeSessionForAuthFail(ctx, SocketMessage.builderWithTimestamp()
					.cmdId(MessageSendCmdEnum.TOKEN_ERR.getCode())
					.message("auth fail").build());
		}
	}

	/**
	 * 发送缓存消息
	 * @param ctx
	 * @param channelUserId
	 */
	private void sendCacheMsg(ChannelHandlerContext ctx, String channelUserId) {
		List<Object> msgs = redisUtil.lGetAll(String.format(CacheConstant.SOCKET_MSG, channelUserId));
		if (msgs != null && msgs.size() > 0) {
			ctx.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(msgs)));
			// XXX 需要判断保证是否发送成功，因为websocket不是可靠连接，这个数据不能丢失；建议是前端收到后发送一个确认标识后删除未读消息
			// redisUtil.del(String.format(CacheConstant.SOCKET_MSG, channelUserId));
		}
	}

	private void closeSessionForAuthFail(ChannelHandlerContext ctx, SocketMessage<?> message) {
		ctx.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(message)));
		ctx.close();
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
	 * 获取用户ID
	 * XXX 更推荐连接后发送一次消息表明身份，不推荐在QueryString添加参数，如果不对 断开链家
	 * @param request 也可以是   FullHttpRequest
	 * @return
	 */
	private String getUserIdFromSession(WebSocketServerProtocolHandler.HandshakeComplete request) {
		String uri = null;
		String[] pathSplit = request.requestUri().split("\\?");
		if (pathSplit.length > 1) {
			uri = pathSplit[1];
		}
		if (uri == null) {
			return null;
		}
		Map<String, String> params = HttpUtil.decodeParamMap(request.requestUri(), StandardCharsets.UTF_8);
		return getUserIdFromToken(params.get(AuthConstant.WEBSOCKET_AUTH_PARAM));
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

}