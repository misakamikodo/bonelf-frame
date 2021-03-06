/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.frame.core.jackson.constraints;

import com.bonelf.frame.core.jackson.StrReplaceDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.annotation.*;

/**
 * 序列化消息，将 指定规则字符串 转为 指定字符串
 * \@StrReplace(from = "[^\\u0000-\\uFFFF]", to = " ") :禁用emoji表情
 * 需搭配 @JsonDeserialize(using = StrReplaceDeserializer.class)使用
 * @author bonelf
 * @date 2020-11-27 09:12:00
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
// 这样没效果 因为JsonDeserialize不是 @Inherited，所以需要打两个注解才行
@com.fasterxml.jackson.annotation.JacksonAnnotation
@JsonDeserialize(using = StrReplaceDeserializer.class)
public @interface StrReplace {
	/**
	 * from regex
	 * @return
	 */
	// @RegEx
	String from();

	/**
	 * to string
	 * @return
	 */
	String to() default "";
}
