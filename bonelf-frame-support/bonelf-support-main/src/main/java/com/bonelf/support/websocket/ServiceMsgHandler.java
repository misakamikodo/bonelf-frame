package com.bonelf.support.websocket;

import com.bonelf.frame.base.util.SpringContextUtils;
import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.frame.core.websocket.constant.MessageRecvCmdEnum;
import com.bonelf.frame.mq.bus.MqProducerService;
import com.bonelf.frame.websocket.property.WebsocketProperties;
import com.bonelf.support.constant.CacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

/**
 * 服务消息分发器
 * @author ccy
 * @date 2021/5/28 11:16
 */
@Slf4j
@Component
public class ServiceMsgHandler {
	@Autowired
	private MqProducerService mqProducerService;
	@Autowired
	private WebsocketProperties websocketProperties;
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;

	/**
	 * 像各服务发送消息
	 * @param msg
	 * @param tagValue 即服务名
	 */
	public void sendMessage2Service(SocketRespMessage msg, String tagValue) {
		if (MessageRecvCmdEnum.CACHE_MSG_RCVED.getCode().equals(msg.getSocketMessage().getCmdId())){
			redisTemplate.delete(String.format(CacheConstant.SOCKET_MSG, msg.getFromUid()));
			return;
		}
		switch (websocketProperties.getTopicType()) {
			case redis:
				redisTemplate.convertAndSend(tagValue, msg);
				break;
			case mq:
				// 发送websocket主题供订阅 tagName是主题名，各服务获取自己主题的消息。获取到消息后通过cmdId判断业务
				mqProducerService.send(websocketProperties.getMqTagPrefix() + tagValue, msg);
				break;
			case feign:
				Object bean = null;
				try {
					bean = SpringContextUtils.getBean(tagValue);
				} catch (BeansException e) {
					log.warn("找不到bean", e);
				}
				if (bean != null) {
					try {
						// 这个需要在pom中引入feign包
						bean.getClass().getMethod("websocketMessage", SocketRespMessage.class).invoke(bean, msg);
					} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						throw new RuntimeException("please add websocket feign method first");
					}
				}
				break;
			default:
		}
	}
}
