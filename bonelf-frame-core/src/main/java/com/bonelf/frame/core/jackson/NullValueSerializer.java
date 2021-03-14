package com.bonelf.frame.core.jackson;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 空值转换
 */
public class NullValueSerializer extends JsonSerializer<Object> {
	@Override
	public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		String fieldName = gen.getOutputContext().getCurrentName();
		try {
			//反射获取字段类型 如果转了下划线这里要转回驼峰取field
			Field field = gen.getCurrentValue().getClass().getDeclaredField(fieldName);
			if (Objects.equals(field.getType(), String.class)) {
				//字符串null返回空字符串
				gen.writeString(StrUtil.EMPTY);
				return;
			} else if (Objects.equals(field.getType(), Collection.class) || field.getType().isArray()) {
				//List字段如果为null,输出为[],而非null
				gen.writeStartArray();
				gen.writeEndArray();
				return;
			} else if (Objects.equals(field.getType(), Map.class)) {
				//map型空值返回{}
				gen.writeStartObject();
				gen.writeEndObject();
				return;
			} else if (Objects.equals(field.getType(), Boolean.class)) {
				//空布尔值返回false
				gen.writeBoolean(false);
				return;
			}
		} catch (NoSuchFieldException ignored) {
		}
		//默认返回""  (是否输出值为null的字段)
		gen.writeString(StrUtil.EMPTY);
	}
}