package com.bonelf.support.websocket.service;

import com.bonelf.frame.core.websocket.SocketRespMessage;

/**
 * 适配mq未引入下的 mq service接口
 * @author bonelf
 * @date 2021/8/4 10:34
 */
public interface MqService {
	/**
	 * 发送消息
	 * @param tag
	 * @param msg
	 */
	void send(String tag, SocketRespMessage msg);
}
