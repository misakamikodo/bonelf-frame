package com.bonelf.frame.web.core.argresolver;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bonelf.frame.web.core.argresolver.databinder.QueryWrapperDataBinder;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * 动态查询参数处理(只支持处理查询，更新删除不予支持)
 * @author bonelf
 * @date 2021/9/16 10:02
 */
public class QueryWrapperArgResolver extends AbstractCustomModelAttributeResolver {

	public QueryWrapperArgResolver() {

	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return (QueryWrapper.class.isAssignableFrom(parameter.getParameterType()));
	}

	@Override
	protected ServletRequestDataBinder createBinder(
			WebDataBinderFactory binderFactory, NativeWebRequest webRequest,
			Object target, String objectName) {
		return new QueryWrapperDataBinder(target, objectName);
	}
}

