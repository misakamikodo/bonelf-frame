package com.bonelf.frame.web.core.argresolver;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bonelf.frame.web.core.argresolver.databinder.PageDataBinder;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * 分页参数处理
 * @author bonelf
 * @date 2021/9/16 10:02
 */
public class PageArgResolver extends AbstractCustomModelAttributeResolver {
	private final Long defaultSize;
	private String[] currentArgs;
	private String[] sizeArgs;

	/**
	 *
	 * @param defaultSize 分页大小
	 * @param currentArgs 当前页合法参数
	 * @param sizeArgs 页面大小合法参数
	 */
	public PageArgResolver(Long defaultSize,
						   String[] currentArgs, String[] sizeArgs) {
		this.defaultSize = defaultSize;
		this.currentArgs = currentArgs;
		this.sizeArgs = sizeArgs;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return (IPage.class.isAssignableFrom(parameter.getParameterType()));
	}

	@Override
	protected ServletRequestDataBinder createBinder(
			WebDataBinderFactory binderFactory, NativeWebRequest webRequest,
			Object target, String objectName) {
		return new PageDataBinder(target, objectName, 0L, defaultSize, currentArgs, sizeArgs);
	}
}

