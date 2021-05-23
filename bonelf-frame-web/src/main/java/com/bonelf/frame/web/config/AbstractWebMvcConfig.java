package com.bonelf.frame.web.config;

import cn.hutool.core.date.DatePattern;
import com.bonelf.frame.base.util.SpringContextUtils;
import com.bonelf.frame.core.jackson.RestObjectMapper;
import com.bonelf.frame.web.core.interceptor.DebugInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * web服务配置
 * //@Autowired
 * //private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;
 * @author bonelf
 **/
@Configuration
public abstract class AbstractWebMvcConfig implements WebMvcConfigurer {
	/**
	 * JsonComponentModule已在JacksonAutoConfiguration中注入 idea报没bean 先不管
	 */
	// @Autowired
	// private JsonComponentModule jsonComponentModule;
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * 拦截器配置
	 * @param registry
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 接口debug拦截器
		if (!SpringContextUtils.isProdProfile()) {
			registry.addInterceptor(new DebugInterceptor())
					.addPathPatterns("/**");
		}
	}

	/**
	 * 消息转换器 配置
	 * @param converters 转化器
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(stringHttpMessageConverter());
		converters.add(jacksonHttpMessageConverter());
	}

	/**
	 * 防止中文乱码的转换器
	 */
	@Bean
	public StringHttpMessageConverter stringHttpMessageConverter() {
		return new StringHttpMessageConverter(StandardCharsets.UTF_8);
	}

	/**
	 * 这样Jackson2ObjectMapperBuilder Bean没改变
	 * 覆盖了 jacksonObjectMapper（@ConditionalOnMissingBean）
	 * 如果不注入自定义的 那么ObjectMapper Bean并没有变，即不是RestObjectMapper；
	 * @return
	 * @see org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration (line 105)
	 */
	@Bean("restObjectMapper")
	// @ConditionalOnBean(JsonComponentModule.class)
	public ObjectMapper restObjectMapper() {
		RestObjectMapper restObjectMapper = new RestObjectMapper();
		restObjectMapper.registerModule(applicationContext.getBean(JsonComponentModule.class));
		return restObjectMapper;
	}

	/**
	 * 通过jackson解析请求
	 * {@link org.springframework.http.converter.json.Jackson2ObjectMapperBuilder spring创建ObjectMapper构造器}
	 * XXX 但注入自定义的ObjectMapper jacksonObjectMapper 又不想使用writerWithDefaultPrettyPrinter，所以又需要new一个最好
	 * XXX new一个没registerModule(jsonComponentModule)会使得通过yml jackson配置和@JsonComponent配置（读取于{@link org.springframework.boot.jackson.JsonComponentModule spring jacksonmodule配置}）的jackson配置失效
	 * @return 转换器
	 * @see org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
	 */
	@Bean
	public MappingJackson2HttpMessageConverter jacksonHttpMessageConverter() {
		MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		// ObjectMapper jacksonObjectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();
		// ObjectMapper jacksonObjectMapper = new RestObjectMapper();
		ObjectMapper jacksonObjectMapper = restObjectMapper();
		// 结果是否格式化
		jacksonObjectMapper.writerWithDefaultPrettyPrinter();
		jackson2HttpMessageConverter.setObjectMapper(jacksonObjectMapper);
		/*
		// 以下配置bean
		objectMapper.writerWithDefaultPrettyPrinter();
		//pretty format
		//this.writerWithDefaultPrettyPrinter();
		objectMapper.setDateFormat(new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN));
		//序列化两边接受方字段不足 不报UnrecognizedPropertyException，只解析对应的
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		//驼峰转下划线
		//this.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE);
		// 字段和值都加引号
		//this.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		objectMapper.getSerializerProvider().setNullValueSerializer(new NullValueSerializer());
		objectMapper.findAndRegisterModules();
		// 空对象转空字符串
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
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
		objectMapper.registerModule(simpleModule);
		*/
		return jackson2HttpMessageConverter;
	}

	/**
	 * 反序列化
	 * LocalDateTime
	 * @return
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
		return builder -> {
			//空字符串转null RestObjectMapper.class 写了
			//JsonSerializer<String> serializer = new StdSerializer<String>(String.class) {
			//	@Override
			//	public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
			//		if (value == null || "".equals(value.trim()) || "null".equals(value) || "undefined".equals(value)) {
			//			gen.writeNull();
			//		}
			//		gen.writeString(value);
			//	}
			//};
			//builder.serializerByType(String.class, serializer);
			builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
		};
	}
}
