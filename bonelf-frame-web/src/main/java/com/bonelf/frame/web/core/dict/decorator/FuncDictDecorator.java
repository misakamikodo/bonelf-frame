package com.bonelf.frame.web.core.dict.decorator;

import cn.hutool.core.util.ReflectUtil;
import com.bonelf.frame.core.dict.annotation.FuncDict;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.web.core.dict.decorator.base.BaseDictDecorator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 静态函数方法字典装饰器
 * String Func.foo(Serializable text)
 * @author bonelf
 * @date 2021/8/11 0:24
 */
@Slf4j
public class FuncDictDecorator extends BaseDictDecorator<FuncDict> {

	@Getter
	private Map<String, Object> annotationValues;

	public FuncDictDecorator(Object target, Field field, FuncDict annotation) {
		super(target, field, annotation);
	}

	public FuncDictDecorator(Object target, Field field, Map<String, Object> annotationValues) {
		super(target, field, null);
		this.annotationValues = annotationValues;
	}

	public FuncDictDecorator(Object target, String field, Map<String, Object> annotationValues) {
		super(target, field, null);
		this.annotationValues = annotationValues;
	}

	@Override
	public void decorate() {
		Object fieldValue = getFieldValue();
		String nameSuffix;
		//翻译字典值对应的txt
		String textValue;
		try {
			if (annotation != null) {
				nameSuffix = annotation.nameSuffix();
				Method method = annotation.value().getMethod(annotation.method(), annotation.methodParamType());
				textValue = ReflectUtil.invokeStatic(method, fieldValue);
			} else {
				nameSuffix = (String)annotationValues.get("nameSuffix");
				Class<?> funcClass = ClassLoader.getSystemClassLoader().loadClass((String)annotationValues.get("value"));
				Class<?> methodParamClass = annotationValues.containsKey("annotationValues")?
						ClassLoader.getSystemClassLoader().loadClass((String)annotationValues.get("methodParamType")):
						String.class;
				Method method = funcClass.getMethod((String)annotationValues.get("method"), methodParamClass);
				textValue = ReflectUtil.invokeStatic(method, fieldValue);
			}
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			throw new BonelfException(e);
		}
		// 翻译字典值对应的txt
		setFieldValue(nameSuffix, textValue);
	}
}
