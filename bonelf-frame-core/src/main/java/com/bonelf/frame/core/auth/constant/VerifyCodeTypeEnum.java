package com.bonelf.frame.core.auth.constant;

import com.bonelf.cicada.enums.CodeValueEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 所有数据库字段枚举
 */
@Getter
@AllArgsConstructor
public enum VerifyCodeTypeEnum implements CodeValueEnum<String> {
	/**
	 *
	 */
	LOGIN("login", "登录"),

	;
	/**
	 * code 唯一code
	 */
	private final String code;
	/**
	 * value 值
	 */
	private final String value;
}