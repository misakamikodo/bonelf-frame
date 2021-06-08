package com.bonelf.support.constant;

import com.bonelf.cicada.enums.CodeValueEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态
 * @author ccy
 * @date 2021/6/7 10:55
 */
@Getter
@AllArgsConstructor
public enum QuartzStatusEnum implements CodeValueEnum<Integer> {
	/**
	 *
	 */
	HEALTHY(0, "正常"),
	STOP(1, "停止")
	;

	private final Integer code;
	private final String value;

	@Override
	public Integer getCode() {
		return code;
	}

	@Override
	public String getValue() {
		return value;
	}
}
