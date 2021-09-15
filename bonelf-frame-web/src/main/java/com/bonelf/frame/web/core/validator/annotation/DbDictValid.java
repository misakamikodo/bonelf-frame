/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.frame.web.core.validator.annotation;

import com.bonelf.frame.web.core.validator.DbDictValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * <p>
 * 数据库字典入参校验（因为@Constraint无法方core包）
 * 微服务下徐调用support服务，没必要一个验证走远程请求，并且core包放不了，feign包就无法使用所以不支持微服务
 * </p>
 * @author bonelf
 * @since 2020/7/9 9:20
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(DbDictValid.List.class)
@Constraint(validatedBy = {DbDictValidator.class})
public @interface DbDictValid {
	/**
	 * valid的参数
	 */
	Class<?>[] groups() default {};

	/**
	 * valid的调用参数
	 */
	Class<? extends Payload>[] payload() default {};

	/**
	 * 异常信息
	 */
	String message() default "{*.validation.constraint.DbDict.message}";


	/**
	 * 字典类型（主表）
	 */
	String dictId();

	/**
	 * Defines several {@link Enum} annotations on the same element.
	 * @see Enum
	 */
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@Retention(RUNTIME)
	@interface List {
		DbDictValid[] value();
	}
}

