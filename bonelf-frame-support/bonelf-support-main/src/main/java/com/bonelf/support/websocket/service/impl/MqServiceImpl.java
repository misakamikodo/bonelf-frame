package com.bonelf.support.websocket.service.impl;

import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.frame.mq.bus.MqProducerService;
import com.bonelf.support.websocket.service.MqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * 适配mq未引入下的 mq service接口
 * @author ccy
 * @date 2021/8/4 10:34
 */
@Service
@ConditionalOnClass(MqProducerService.class)
public class MqServiceImpl implements MqService {
	@Autowired(required = false)
	private MqProducerService mqProducerService;

	@Override
	public void send(String tag, SocketRespMessage msg) {
		mqProducerService.send(tag, msg);
	}
}
