/*
 * Copyright (c) 2021. Bonelf.
 */

package com.bonelf.frame.web.core.advice;

import com.bonelf.frame.base.util.SpringContextUtils;
import com.bonelf.frame.core.domain.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * <p>
 * 控制器返回数据
 * annotation和basePackages同时定义取了并集，所以不使用 annotations = {RestController.class, ResponseBody.class},
 * 为了跳过/v2/api-docs类似三方接口（XXX 可以尝试仅处理/api 开头的url）
 * TODO 字典解析移到这，profile为prod关闭devMessage
 *
 * </p>
 * @author bonelf
 * @since 2021/1/13 14:55
 */
@RestControllerAdvice(
		basePackages = "com.bonelf.**.controller"
)
public class RestControllerResultAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(@NonNull MethodParameter methodParameter,
							@NonNull Class<? extends HttpMessageConverter<?>> aClass) {
		// String类型(stringHttpMessageConverter)不支持
		// java.lang.ClassCastException: com.bonelf.frame.core.domain.Result cannot be cast to java.lang.String(因为还是走的stringHttpMessageConverter)
		// 这里只对可序列化Json做处理
		return MappingJackson2HttpMessageConverter.class.isAssignableFrom(aClass);
	}

	@Override
	public Object beforeBodyWrite(Object body,
								  @NonNull MethodParameter methodParameter,
								  @NonNull MediaType mediaType,
								  @NonNull Class<? extends HttpMessageConverter<?>> aClass,
								  @NonNull ServerHttpRequest serverHttpRequest,
								  @NonNull ServerHttpResponse serverHttpResponse) {
		Result<?> result;
		if (body instanceof Result) {
			result = (Result<?>)body;
		} else {
			result = Result.ok(body);
		}
		// JsonNode result = objectMapper.valueToTree(target);
		if (SpringContextUtils.isProdProfile()) {
			result.setDevMessage(null);
		}
		return result;
	}
}