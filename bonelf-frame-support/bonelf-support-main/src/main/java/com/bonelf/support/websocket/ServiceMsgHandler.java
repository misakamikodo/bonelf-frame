package com.bonelf.support.websocket;

import com.bonelf.frame.cloud.security.constant.AuthFeignConstant;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.frame.core.websocket.constant.MessageRecvCmdEnum;
import com.bonelf.frame.mq.bus.MqProducerService;
import com.bonelf.frame.websocket.property.WebsocketProperties;
import com.bonelf.support.constant.CacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 服务消息分发器
 * @author bonelf
 * @date 2021/5/28 11:16
 */
@Slf4j
@Component
public class ServiceMsgHandler {
	@Autowired(required = false)
	private MqProducerService mqProducerService;
	@Autowired
	private WebsocketProperties websocketProperties;
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;
	@Autowired
	private RestTemplate restTemplate;
	@Value("${server.servlet.context-path:}")
	private String ctxPath;

	/**
	 * 像各服务发送消息
	 * @param msg
	 * @param tagValue 即服务名
	 */
	public void sendMessage2Service(SocketRespMessage msg, String tagValue) {
		if (MessageRecvCmdEnum.CACHE_MSG_RCVED.getCode().equals(msg.getSocketMessage().getCmdId())) {
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
				String url = "http://" + tagValue + ctxPath + "/websocketMessage";
				HttpHeaders headers = new HttpHeaders();
				headers.set(AuthFeignConstant.AUTH_HEADER, AuthFeignConstant.FEIGN_REQ_FLAG_PREFIX + " -");
				headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				HttpEntity<SocketRespMessage> request = new HttpEntity<>(msg, headers);
				ResponseEntity<Result> response;
				try {
					response = restTemplate.postForEntity(url, request, Result.class);
				} catch (RestClientException e) {
					log.error("消息转发失败", e);
				}
				break;
			default:
		}
	}
}
