package com.bonelf.frame.web.aop;

import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.web.constant.ResultCostAttr;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Result Cost填充
 * @author Bonelf
 **/
@Aspect
@Component
@Slf4j
public class ResultCostAspect {
	private final ThreadLocal<Long> timestamp;

	public ResultCostAspect() {
		this.timestamp = new ThreadLocal<>();
	}

	@Pointcut("execution(public * com.bonelf..*.*Controller.*(..))")
	public void pointCut() {
	}

	// 定义切点Pointcut
	@Before("pointCut()")
	public void before() {
		timestamp.set(System.currentTimeMillis());
	}

	/**
	 * @see com.bonelf.frame.web.core.advice.RestControllerResultAdvice
	 * @param rst
	 */
	@AfterReturning(pointcut = "pointCut()", returning = "rst")
	public void after(Object rst) {
		Result<?> result;
		long cost = System.currentTimeMillis() - timestamp.get();
		if (rst instanceof Result) {
			result = (Result<?>)rst;
			result.setCost(cost);
		} else {
			// 1通过代理给对象绑定cost 在ResultAdvice通过反射获取
			// 2 通过attribute
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
				HttpServletRequest request = attributes.getRequest();
				request.setAttribute(ResultCostAttr.COST, cost);
			}
		}
		timestamp.remove();
	}

}
