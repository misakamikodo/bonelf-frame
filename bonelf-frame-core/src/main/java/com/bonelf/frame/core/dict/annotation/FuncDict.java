package com.bonelf.frame.core.dict.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 静态方法字典
 * </p>
 * @author bonelf
 * @since 2020/10/11 17:45
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FuncDict {
	/**
	 *  静态方法工具类
	 */
	Class<?> value();
	/**
	 *  获取方法
	 */
	String method();

	/**
	 * 参数类型
	 * @return
	 */
	Class<?> methodParamType() default String.class;

	/**
	 * 在前端解析时返回对应枚举值 不设置代表不返回
	 */
	String nameSuffix() default "Name";
}
