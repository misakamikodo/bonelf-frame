package com.bonelf.frame.web.config;

import cn.hutool.core.date.DatePattern;
import com.bonelf.frame.base.util.SpringContextUtils;
import com.bonelf.frame.core.jackson.RestObjectMapper;
import com.bonelf.frame.web.core.argresolver.PageArgResolver;
import com.bonelf.frame.web.core.argresolver.QueryWrapperArgResolver;
import com.bonelf.frame.web.core.interceptor.DebugInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

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
	@Autowired
	private ApplicationContext applicationContext;
	@Value("${bonelf.page.default-size:10}")
	private Long defaultPageSize;
	@Value("${bonelf.page.default-size:current,page}")
	private String[] currentPageArgs;
	@Value("${bonelf.page.default-size:size,pageSize,limit,rows}")
	private String[] sizePageArgs;

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

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new PageArgResolver(defaultPageSize, currentPageArgs, sizePageArgs));
		try {
			if (Objects.requireNonNull(applicationContext.getClassLoader())
					.loadClass("com.baomidou.mybatisplus.extension.plugins.pagination.Page") != null) {
				resolvers.add(new QueryWrapperArgResolver());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 消息转换器 配置
	 * @param converters 转化器
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(applicationContext.getBean(StringHttpMessageConverter.class));
		converters.add(applicationContext.getBean(MappingJackson2HttpMessageConverter.class));
		// converters.add(stringHttpMessageConverter());
		// converters.add(jacksonHttpMessageConverter());
	}

	/**
	 * 防止中文乱码的转换器
	 */
	@Bean
	public StringHttpMessageConverter stringHttpMessageConverter() {
		return new StringHttpMessageConverter(StandardCharsets.UTF_8);
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
	public MappingJackson2HttpMessageConverter jacksonHttpMessageConverter(ObjectMapper jacksonObjectMapper) {
		MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		// ObjectMapper jacksonObjectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();
		// ObjectMapper jacksonObjectMapper = new RestObjectMapper();
		// ObjectMapper jacksonObjectMapper = restObjectMapper();
		// 结果是否格式化
		jacksonObjectMapper.writerWithDefaultPrettyPrinter();
		jackson2HttpMessageConverter.setObjectMapper(jacksonObjectMapper);
		return jackson2HttpMessageConverter;
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
