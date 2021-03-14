package com.bonelf.support.websocket;

import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.frame.base.util.SpringContextUtils;
import com.bonelf.frame.websocket.property.WebsocketProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

@Component
public class ServiceMsgHandler {
	@Autowired
	private WebsocketProperties websocketProperties;
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;

	/**
	 * 像各服务发送消息
	 * @param msg
	 * @param topicName
	 */
	public void sendMessage2Service(SocketRespMessage msg, String topicName) {
		switch (websocketProperties.getTopicType()){
			case redis:
				redisTemplate.convertAndSend(topicName, msg);
				break;
			case mq:
				// mqProducerService.send(websocketProperties.getMqTopic()", topicName,
				// 		msg);
				break;
			case feign:
				for (Class<?> clazz : this.getFeignClientBeanClassByTopicName(topicName)) {
					Object bean = SpringContextUtils.getBean(clazz);
					try {
						clazz.getMethod("websocketMessage", SocketRespMessage.class).invoke(bean, msg);
					} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						throw new RuntimeException("please add websocket feign method first");
					}
				}
				break;
			default:
		}
	}

	/**
	 * TODO 根据主题获取所有Bean类，这里要引入所有会用到的feign包，所以不妥，推荐使用mq，这里保留方法
	 * stomp 请在@MessageMapping中执行实现feign，mq的话stomp已实现{@link ??}
	 * @param topicName
	 * @return
	 */
	private Class<?>[] getFeignClientBeanClassByTopicName(String topicName) {
		String[] channels = websocketProperties.getCmdChannels().get(topicName);
		// 根据channels获取所有bean
		Set<Class<?>> result = new HashSet<>();
		for (String channel : channels) {
			switch (channel){
				case "demo":
					break;
				default:
			}
		}
		return result.toArray(new Class<?>[0]);
	}
}
