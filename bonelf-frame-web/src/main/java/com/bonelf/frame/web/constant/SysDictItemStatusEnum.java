package com.bonelf.frame.web.constant;

import com.bonelf.cicada.enums.CodeValueEnum;

/**
 * 字典状态字典
 * @author bonelf
 * @date 2021/6/14 20:01
 */
public enum SysDictItemStatusEnum implements CodeValueEnum<Long> {
	/**
	 *
	 */
	FREEZE(1L, "禁用"),
	HEALTHY(0L, "启用");

	private final Long code;
	private final String value;

	SysDictItemStatusEnum(long code, String value) {
		this.code = code;
		this.value = value;
	}

	@Override
	public Long getCode() {
		return code;
	}

	@Override
	public String getValue() {
		return value;
	}
}
