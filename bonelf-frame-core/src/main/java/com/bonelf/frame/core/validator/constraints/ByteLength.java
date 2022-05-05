/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.frame.core.validator.constraints;

import com.bonelf.frame.core.validator.ByteLengthValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字节长度校验
 * 参考 {@link org.hibernate.validator.constraints.Length}
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = ByteLength.List.class)
@Constraint(
		validatedBy = {ByteLengthValidator.class}
)
public @interface ByteLength {
	int min() default 0;

	int max() default 2147483647;

	String charset() default "UTF-8";

	String message() default "{org.hibernate.validator.constraints.Length.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
	@Retention(RetentionPolicy.RUNTIME)
	@interface List {
		ByteLength[] value();
	}
}
