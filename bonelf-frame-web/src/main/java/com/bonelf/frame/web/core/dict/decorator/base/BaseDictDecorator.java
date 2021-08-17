package com.bonelf.frame.web.core.dict.decorator.base;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * 基础装饰器
 * @author bonelf
 * @date 2021/8/8 16:45
 */
@Slf4j
public abstract class BaseDictDecorator<A extends Annotation> {
	/**
	 * 标有此注解（annotation）的对象
	 */
	protected final Object target;

	/**
	 * 此对象的 Field
	 */
	protected Field field;

	/**
	 * target为map是 field 为 null 取mapField; 必须是Map&lt;String, Object>
	 */
	protected String mapField;

	/**
	 * 字典注解
	 */
	protected final A annotation;

	protected BaseDictDecorator(Object target, Field field, A annotation) {
		this.target = target;
		this.field = field;
		this.annotation = annotation;
	}

	protected BaseDictDecorator(Object target, String mapField, A annotation) {
		this.target = target;
		this.mapField = mapField;
		this.annotation = annotation;
	}

	/**
	 * 装饰
	 * @return
	 */
	protected abstract void decorate();

	public Object getFieldValue() {
		if (target instanceof Map) {
			Map<?, ?> mapTarget = (Map<?, ?>)target;
			return mapTarget.get(mapField);
		} else {
			return ReflectUtil.getFieldValue(target, field == null ? mapField : field.getName());
		}
	}

	@SuppressWarnings("unchecked")
	public void setFieldValue(String nameSuffix, String textValue) {
		try {
			if (target instanceof Map) {
				Map<String, Object> mapTarget = (Map<String, Object>)target;
				mapTarget.put(mapField + nameSuffix, textValue);
			} else {
				ReflectUtil.setFieldValue(target, (field == null ? mapField : field.getName()) + nameSuffix, textValue);
			}
		} catch (UtilException | IllegalArgumentException e) {
			log.warn("对象需要转字典的对应Field找不到：{}，请检查类型名称是否正确和类型是否为String",
					field.getName() + nameSuffix);
		}
	}
}
