package com.bonelf.frame.core.websocket.constant;

import com.bonelf.cicada.enums.CodeValueEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OnlineStatusEnum implements CodeValueEnum {
	/**
	 * 连接成功响应
	 */
	ONLINE(0, "在线"),
	OFFLINE(1, "下线")
	;
	private Integer code;

	private String value;
}
