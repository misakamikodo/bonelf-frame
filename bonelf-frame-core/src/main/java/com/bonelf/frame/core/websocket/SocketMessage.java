package com.bonelf.frame.core.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息的实体
 * 响应、请求同一个类
 * @author bonelf
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocketMessage<T> {
	/**
	 * 指令类型
	 * 根据具体项目确定
	 * 根据具体项目确定
	 * @see com.bonelf.frame.core.websocket.constant.MessageSendCmdEnum
	 * @see com.bonelf.frame.core.websocket.constant.MessageRecvCmdEnum
	 */
	private Integer cmdId;
	/**
	 * 响应：发向的服务 根据具体项目确定
	 * 即 MQ 的 tagName 接收方服务名
	 * stomp 通过url确定渠道（单渠道的话），否则还是使用这个。
	 */
	private String[] channel;
	/**
	 * 请求:发向的用户
	 * 响应:转发的用户
	 * 单发/群发消息 ","使用，分割字符串，避免类型requestBody转换问题
	 */
	private String userIds;
	/**
	 * 响应：字符串弹窗消息
	 * 请求：消息
	 */
	private String message;
	/**
	 * 消息体
	 */
	private T data;
	/**
	 * 秒时间戳
	 */
	private String timestamp;

	/**
	 * 创建发送给用户端的消息对象
	 * @return
	 */
	public SocketMessage<?> buildMsg() {
		this.timestamp = String.valueOf(System.currentTimeMillis() / 1000);
		return this;
	}

	public static <T> SocketMessageBuilder<T> builderWithTimestamp() {
		return SocketMessage.<T>builder().timestamp(String.valueOf(System.currentTimeMillis() / 1000));
	}
}
