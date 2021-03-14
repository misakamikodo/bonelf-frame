package com.bonelf.frame.base.util;

import cn.hutool.core.bean.BeanUtil;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;

import java.util.Map;

/**
 * <p>
 * socket工具类 仅用于本项目socket实现
 * </p>
 * @author bonelf
 * @since 2020/10/20 23:23
 */
public class SocketUtil {

	/**
	 * socket对象
	 * @param data 对象
	 * @param clazz 转化类
	 * @param <T> 类型
	 * @return 信息
	 */
	public static <T> T parseSocketData(Map<String, Object> data, Class<T> clazz) {
		if (data == null) {
			throw new BonelfException(CommonBizExceptionEnum.REQUEST_NULL);
		}
		return BeanUtil.toBean(data, clazz);
	}
}
