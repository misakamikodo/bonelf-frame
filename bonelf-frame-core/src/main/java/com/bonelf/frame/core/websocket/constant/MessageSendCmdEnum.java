package com.bonelf.frame.core.websocket.constant;

import lombok.Getter;

@Getter
public enum MessageSendCmdEnum {
	/**
	 * 心跳测试
	 */
	PING_PONG(1, "心跳测试"),

	/**
	 * 错误消息
	 */
	ERR_MSG(2, "错误消息"),

	/**
	 * 问候消息
	 */
	GREET(3, "问候消息"),
	TOKEN_ERR(4, "token错误"),
	;
	private Integer code;
	private String desc;

	MessageSendCmdEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}
}
