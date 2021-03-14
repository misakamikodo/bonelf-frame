package com.bonelf.frame.core.exception;

/**
 * <p>
 * 服务异常枚举接口
 * </p>
 * @author bonelf
 * @since 2020/10/11 17:35
 */
public interface AbstractBaseExceptionEnum {
	/**
	 * 状态码
	 * @return
	 */
	String getCode();

	/**
	 * 弹窗错误消息
	 * @return
	 */
	String getMessage();

	/**
	 * 开发提示消息
	 * @return
	 */
	String getDevMessage();
}
