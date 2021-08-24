/*
 * Copyright (c) 2020. Bonelf.
 */

package com.bonelf.frame.web.util;

import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.AuthExceptionEnum;
import com.bonelf.frame.web.security.domain.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 账号管理
 * </p>
 * @see com.bonelf.frame.web.security.converter.JwtWithUserInfoAccessTokenConverter 添加更多信息到principal
 * @author Chenyuan
 * @since 2020/12/31 16:32
 */
@Component
public class ContextUtil {

	/**
	 * 获得用户
	 * @return
	 */
	public AuthUser getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			throw new BonelfException(AuthExceptionEnum.INVALID_TOKEN);
		}
		AuthUser user = (AuthUser)authentication.getPrincipal();
		return user;
	}

	/**
	 * 获得用户编号
	 * @return
	 */
	public Long getUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			throw new BonelfException(AuthExceptionEnum.INVALID_TOKEN);
		}
		AuthUser user = (AuthUser)authentication.getPrincipal();
		if (user == null || user.getUserId() == null) {
			throw new BonelfException(AuthExceptionEnum.INVALID_TOKEN);
		}
		return user.getUserId();
	}

	/**
	 * 获得用户编号
	 * @return
	 */
	public Long getUserIdCanNull() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		AuthUser user = (AuthUser)authentication.getPrincipal();
		if (user == null || user.getUserId() == null) {
			return null;
		}
		return user.getUserId();
	}
}
