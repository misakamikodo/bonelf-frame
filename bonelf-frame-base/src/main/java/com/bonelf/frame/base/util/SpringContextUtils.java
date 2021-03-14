package com.bonelf.frame.base.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class SpringContextUtils implements ApplicationContextAware {

	/**
	 * 上下文对象实例
	 */
	private static ApplicationContext applicationContext;

	/**
	 * 生产环境？
	 * @return
	 */
	public static boolean isProdProfile() {
		for (String activeProfile : applicationContext.getEnvironment().getActiveProfiles()) {
			if ("prod".equalsIgnoreCase(activeProfile.trim())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
		SpringContextUtils.applicationContext = applicationContext;
	}

	/**
	 * 获取applicationContext
	 * @return
	 */
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * 获取HttpServletRequest
	 */
	public static HttpServletRequest getHttpServletRequest() {
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			throw new RuntimeException("无法获取request，请检查bean是否注入");
		}
		return attributes.getRequest();
	}

	public static String getDomain() {
		HttpServletRequest request = getHttpServletRequest();
		StringBuffer url = request.getRequestURL();
		return url.delete(url.length() - request.getRequestURI().length(), url.length()).toString();
	}

	public static String getOrigin() {
		HttpServletRequest request = getHttpServletRequest();
		return request.getHeader(HttpHeaders.ORIGIN);
	}

	/**
	 * 通过name获取 Bean.
	 * @param name
	 * @return
	 */
	public static Object getBean(String name) throws BeansException {
		return getApplicationContext().getBean(name);
	}

	/**
	 * 通过class获取Bean.
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T> T getBean(Class<T> clazz) throws BeansException {
		return getApplicationContext().getBean(clazz);
	}

	/**
	 * 通过name,以及Clazz返回指定的Bean
	 * @param name
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T> T getBean(String name, Class<T> clazz) throws BeansException {
		return getApplicationContext().getBean(name, clazz);
	}
}
