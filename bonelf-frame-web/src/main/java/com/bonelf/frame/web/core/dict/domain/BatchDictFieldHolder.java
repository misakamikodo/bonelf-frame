package com.bonelf.frame.web.core.dict.domain;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.ReflectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * 用于翻译字典用的字典信息记录对象
 * @author bonelf
 * @date 2021/8/8 17:31
 */
@Getter
@Setter
@Slf4j
@AllArgsConstructor
public class BatchDictFieldHolder<T extends Annotation> {
	/**
	 * 标有此注解（annotation）的对象
	 */
	private final Object target;

	/**
	 * 此对象的 Field
	 */
	private Field field;

	/**
	 * map 对象的key
	 */
	protected String mapField;

	/**
	 * 字典注解
	 * 类型使用{@link com.bonelf.frame.web.core.dict.decorator.base.BaseBatchDictDecorator}
	 */
	private T annotation;

	private Map<String, Object> annotationValues;

	public BatchDictFieldHolder(Object target, T annotation) {
		this.target = target;
		this.annotation = annotation;
	}

	public BatchDictFieldHolder(Object target, Field field, T annotation) {
		this.target = target;
		this.field = field;
		this.annotation = annotation;
	}

	public BatchDictFieldHolder(Object target, Field field, Map<String, Object> annotationValues) {
		this.target = target;
		this.field = field;
		this.annotationValues = annotationValues;
	}

	public BatchDictFieldHolder(Object target, String mapField, T annotation) {
		this.target = target;
		this.mapField = mapField;
		this.annotation = annotation;
	}

	public BatchDictFieldHolder(Object target, String mapField, Map<String, Object> annotationValues) {
		this.target = target;
		this.mapField = mapField;
		this.annotationValues = annotationValues;
	}

	public Object getFieldValue() {
		if (field != null) {
			return ReflectUtil.getFieldValue(target, field.getName());
		} else {
			Map<?, ?> mapTarget = (Map<?, ?>)target;
			return mapTarget.get(mapField);
		}
	}

	@SuppressWarnings("unchecked")
	public void setFieldValue(String nameSuffix, String textValue) {
		try {
			if (field != null) {
				ReflectUtil.setFieldValue(target, field.getName() + nameSuffix, textValue);
			} else {
				Map<String, Object> mapTarget = (Map<String, Object>)target;
				mapTarget.put(mapField, textValue);
			}
		} catch (UtilException | IllegalArgumentException e) {
			log.warn("对象需要转字典的对应Field找不到：{}，请检查类型名称是否正确和类型是否为String",
					field.getName() + nameSuffix);
		}
	}
}
