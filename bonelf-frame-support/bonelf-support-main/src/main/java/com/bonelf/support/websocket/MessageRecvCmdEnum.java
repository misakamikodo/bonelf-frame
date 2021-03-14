package com.bonelf.support.websocket;


import com.bonelf.cicada.enums.CodeValueEnum;
import lombok.Getter;

/**
 * 现在只需要将对应关系定义在yml中即可；
 * mq则只需要通过@StreamListener接受消息即可；
 * 不会涉及消息cmd类型
 */
@Deprecated
@Getter
public enum MessageRecvCmdEnum implements CodeValueEnum<Integer> {
	/**
	 * 用户连接提示
	 */
	INIT_CALLBACK(0, "连接成功提示"),

	/**
	 * 心跳测试
	 */
	PING_PONG(1, "心跳测试"),

	/**
	 * 普通测试
	 */
	TEST(2, "普通测试"),
	;
	private final Integer code;
	private final String value;

	MessageRecvCmdEnum(Integer code, String desc) {
		this.code = code;
		this.value = desc;
	}
}
