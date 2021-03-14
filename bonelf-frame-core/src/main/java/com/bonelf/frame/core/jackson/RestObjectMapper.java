/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.frame.core.jackson;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 自定义ObjectMapper <br />
 * 使用
 * <br />
 * <code>@Autowired<br />
 * private ObjectMapper objectMapper
 * </code><br />
 * 代替
 * @author bonelf
 */
@Slf4j
public class RestObjectMapper extends ObjectMapper {
	public RestObjectMapper() {
		//pretty format
		//this.writerWithDefaultPrettyPrinter();
		this.setDateFormat(new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN));
		//序列化两边接受方字段不足 不报UnrecognizedPropertyException，只解析对应的
		this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		//驼峰转下划线
		//this.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE);
		// 字段和值都加引号
		//this.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		this.getSerializerProvider().setNullValueSerializer(new NullValueSerializer());
		this.findAndRegisterModules();
		/*
		 * 序列换成json时,将所有的long变成string
		 * 因为js中得数字类型不能包含所有的java long值
		 */
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
		simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
		//simpleModule.addSerializer(Double.TYPE, ToStringSerializer.instance);
		//simpleModule.addSerializer(Double.class, ToStringSerializer.instance);
		simpleModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
		//旧：DateTimeFormatter.ISO_DATE_TIME
		simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
		//旧：DateTimeFormatter.ISO_DATE
		//simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
		//旧：DateTimeFormatter.ISO_TIME
		simpleModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN)));
		//空转null
		JsonDeserializer<String> serializer = new StdDeserializer<String>(String.class) {
			@Override
			public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
				String value = StringDeserializer.instance.deserialize(p, ctxt);
				if (value == null || "".equals(value.trim()) || "null".equals(value) || "undefined".equals(value)) {
					return null;
				}
				return value;
			}
		};
		simpleModule.addDeserializer(String.class, serializer);
		// 空对象转空字符串
		this.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		this.registerModule(simpleModule);
	}
}
