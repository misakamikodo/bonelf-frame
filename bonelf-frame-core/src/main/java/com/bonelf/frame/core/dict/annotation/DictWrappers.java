package com.bonelf.frame.core.dict.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 用于支持 Map、泛型 等不确定类型的字段翻译
 * </p>
 * @author bonelf
 * @since 2020/10/11 17:45
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DictWrappers {
	DictWrapper[] value();
}
