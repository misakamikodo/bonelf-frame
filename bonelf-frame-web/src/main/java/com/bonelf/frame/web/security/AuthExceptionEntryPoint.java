package com.bonelf.frame.web.security;

import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.base.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * 异常封装
 * @see AuthenticationEntryPoint
 * </p>
 * @author bonelf
 * @since 2020/11/21 11:53
 */
@Slf4j
public class AuthExceptionEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
						 AuthenticationException authException) throws ServletException {
		//Throwable cause = authException.getCause();
		response.setStatus(HttpStatus.OK.value());
			response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
			Result<?> result;
			try {
			if (authException instanceof InsufficientAuthenticationException) {
				log.warn("用户凭据非法:{}", authException.getMessage());
				result = Result.error(CommonBizExceptionEnum.INVALID_TOKEN);
			} else {
				result = Result.error(CommonBizExceptionEnum.INVALID_TOKEN.getCode(), authException.getMessage());
			}
			response.getWriter().write(JsonUtil.toJson(result));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

