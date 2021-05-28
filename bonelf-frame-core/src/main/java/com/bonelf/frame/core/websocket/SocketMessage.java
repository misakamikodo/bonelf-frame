package com.bonelf.frame.core.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息的实体
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocketMessage<T> {
	/**
	 * 指令类型
	 * 根据具体项目确定
	 */
	private Integer cmdId;
	/**
	 * 发向的服务
	 * 根据具体项目确定
	 * 即 MQ 的 tagName
	 * stomp 通过url确定渠道（单渠道的话），否则还是使用这个。
	 */
	private String[] channel;
	/**
	 * 单发/群发消息 ","使用，分割字符串，避免类型requestBody转换问题
	 */
	private String userIds;
	/**
	 * 消息体
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
}
