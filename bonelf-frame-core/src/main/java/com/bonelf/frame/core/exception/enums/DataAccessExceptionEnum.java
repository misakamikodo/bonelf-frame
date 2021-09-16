package com.bonelf.frame.core.exception.enums;

import com.bonelf.frame.core.exception.AbstractBaseExceptionEnum;
import lombok.Getter;

/**
 * <p>
 * 公用异常信息
 * 401 认证异常 400 接口传参异常
 * </p>
 * @author bonelf
 * @since 2020/10/12 10:48
 */
@Getter
public enum DataAccessExceptionEnum implements AbstractBaseExceptionEnum {
	/**
	 *
	 */
	COMMON("505", "系统异常","数据库操作异常：%s");

	DataAccessExceptionEnum(String code, String message, String devMessage) {
		this.code = code;
		this.message = message;
		this.devMessage = devMessage;
	}

	DataAccessExceptionEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * 服务状态码 版本号+模块号+序号 类似A0101
	 */
	private final String code;
	/**
	 * 弹窗异常信息
	 */
	private final String message;
	/**
	 * 异常信息
	 */
	private String devMessage;
}
