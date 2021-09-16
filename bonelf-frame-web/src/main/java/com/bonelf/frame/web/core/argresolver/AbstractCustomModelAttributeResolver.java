package com.bonelf.frame.web.core.argresolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.ServletRequest;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 抽象参数处理
 * @author bonelf
 * @date 2021/9/16 10:04
 */
@Slf4j
public abstract class AbstractCustomModelAttributeResolver implements HandlerMethodArgumentResolver {

	public AbstractCustomModelAttributeResolver() {
	}


	@Override
	public final Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
										@NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		String name = Conventions.getVariableNameForParameter(parameter);
		Object attribute = (mavContainer != null && mavContainer.containsAttribute(name) ? mavContainer.getModel().get(name) :
				createAttribute(name, parameter, binderFactory, webRequest));

		//WebDataBinder binder = binderFactory.createBinder(webRequest, attribute, name);
		ServletRequestDataBinder binder = createBinder(binderFactory, webRequest, attribute, name);
		if (binder.getTarget() != null) {
			if (mavContainer != null && !mavContainer.isBindingDisabled(name)) {
				bindRequestParameters(binder, webRequest);
			}
			validateIfApplicable(binder, parameter);
			if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
				throw new BindException(binder.getBindingResult());
			}
		}

		// Add resolved attribute and BindingResult at the end of the model
		Map<String, Object> bindingResultModel = binder.getBindingResult().getModel();

		if (mavContainer != null) {
			mavContainer.removeAttributes(bindingResultModel);
			mavContainer.addAllAttributes(bindingResultModel);
		}

		return binder.convertIfNecessary(binder.getTarget(), parameter.getParameterType(), parameter);
	}


	protected void bindRequestParameters(ServletRequestDataBinder servletBinder, NativeWebRequest request) {
		ServletRequest servletRequest = request.getNativeRequest(ServletRequest.class);
		if (servletRequest != null) {
			servletBinder.bind(servletRequest);
		}
	}

	protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
		Annotation[] annotations = parameter.getParameterAnnotations();
		for (Annotation ann : annotations) {
			Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
			if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
				Object hints = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
				Object[] validationHints = (hints instanceof Object[] ? (Object[])hints : new Object[]{hints});
				binder.validate(validationHints);
				break;
			}
		}
	}


	protected boolean isBindExceptionRequired(WebDataBinder binder, MethodParameter parameter) {
		int i = parameter.getParameterIndex();
		if (parameter.getMethod() != null) {
			Class<?>[] paramTypes = parameter.getMethod().getParameterTypes();
			boolean hasBindingResult = (paramTypes.length > (i + 1) && Errors.class.isAssignableFrom(paramTypes[i + 1]));
			return !hasBindingResult;
		}
		return true;
	}


	protected Object createAttribute(String attributeName, MethodParameter parameter,
									 WebDataBinderFactory binderFactory, NativeWebRequest webRequest) throws Exception {
		return BeanUtils.instantiateClass(parameter.getParameterType());
	}

	protected abstract ServletRequestDataBinder createBinder(WebDataBinderFactory binderFactory, NativeWebRequest webRequest, Object target, String objectName) throws Exception;
}