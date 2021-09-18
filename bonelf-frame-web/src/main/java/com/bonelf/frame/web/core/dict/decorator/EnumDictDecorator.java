package com.bonelf.frame.web.core.dict.decorator;

import com.bonelf.cicada.enums.CodeValueEnum;
import com.bonelf.cicada.util.EnumUtil;
import com.bonelf.frame.core.dict.constraints.EnumDict;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.web.core.dict.decorator.base.BaseDictDecorator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 枚举字典装饰器
 * @author bonelf
 * @date 2021/8/11 0:20
 */
@Slf4j
public class EnumDictDecorator extends BaseDictDecorator<EnumDict> {

	@Getter
	private Map<String, Object> annotationValues;

	public EnumDictDecorator(Object target, Field field, EnumDict annotation) {
		super(target, field, annotation);
	}

	public EnumDictDecorator(Object target, Field field, Map<String, Object> annotationValues) {
		super(target, field, null);
		this.annotationValues = annotationValues;
	}

	public EnumDictDecorator(Object target, String field, Map<String, Object> annotationValues) {
		super(target, field, null);
		this.annotationValues = annotationValues;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void decorate() {
		Object fieldValue = getFieldValue();
		String nameSuffix;
		//翻译字典值对应的txt
		String textValue;
		if (annotation != null) {
			nameSuffix = annotation.nameSuffix();
			textValue = EnumUtil.getEnumString(fieldValue, annotation.value());
		} else {
			nameSuffix = (String)annotationValues.get("nameSuffix");
			try {
				textValue = EnumUtil.getEnumString(fieldValue,
						(Class<? extends CodeValueEnum<String>>)ClassLoader.getSystemClassLoader()
								.loadClass((String)annotationValues.get("value")));
			} catch (ClassNotFoundException e) {
				throw new BonelfException(e);
			}
		}
		setFieldValue( nameSuffix, textValue);
	}
}
