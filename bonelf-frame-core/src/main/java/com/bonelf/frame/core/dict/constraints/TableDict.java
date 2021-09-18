package com.bonelf.frame.core.dict.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 数据库字典
 * </p>
 * @author bonelf
 * @since 2020/10/11 17:44
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableDict {
	/**
	 *  tableEntityClass extends BaseEntity
	 */
	Class<?> value() default void.class;

	/**
	 * 翻译前的名称
	 */
	String key() default "id";

	/**
	 * 翻译后的名称
	 */
	String val() default "name";

	/**
	 * 当设置了sql table、key、val 都会失效而取sql
	 * example: SELECT id `key`, name `val` FROM example WHERE id IN (:values)
	 */
	String sql() default "";

	/**
	 * 是否使用定时的缓存
	 * （不会实时修改，但是能减小数据库服务压力，提高访问速度）
	 */
	boolean cached() default true;

	/**
	 * 在前端解析时返回对应枚举值 不设置代表不返回
	 */
	String nameSuffix() default "Name";
}
