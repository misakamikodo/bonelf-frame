package com.bonelf.frame.core.constant.enums;

import com.bonelf.cicada.enums.CodeValueEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FreezeEnum implements CodeValueEnum<Integer> {
	/**
	 *
	 */
	FREEZE(1, "冻结"),

	ENABLE(0, "启用"),
	;
	/**
	 * code 唯一code
	 */
	private Integer code;
	/**
	 * value 值
	 */
	private String value;
}
