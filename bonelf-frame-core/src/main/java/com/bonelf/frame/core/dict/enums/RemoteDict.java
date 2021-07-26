package com.bonelf.frame.core.dict.enums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 枚举字典
 * 通常如“订单状态”这类 用户不关心，而代码经常使用其值 的存到枚举字典
 * </p>
 * @author bonelf
 * @since 2020/10/11 17:45
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteDict {
	/**
	 *  地址url 使用restTemplate Bean cloud支持loadBalance
	 */
	String value();
	/**
	 * 在前端解析时返回对应枚举值 不设置代表不返回
	 */
	String nameSuffix() default "Name";
	/**
	 * 是否使用定时的缓存
	 * （不会实时修改，但是能减小数据库服务压力，提高访问速度）
	 */
	boolean cached() default true;
}
