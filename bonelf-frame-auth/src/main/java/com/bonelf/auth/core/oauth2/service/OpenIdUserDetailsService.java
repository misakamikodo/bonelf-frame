/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.auth.core.oauth2.service;

import cn.hutool.core.collection.CollectionUtil;
import com.bonelf.auth.core.oauth2.granter.openid.OpenIdTokenGranter;
import com.bonelf.frame.core.auth.domain.User;
import com.bonelf.frame.core.constant.UsernameType;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.web.security.domain.AuthUser;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * 微信登录校验
 * @author bonelf
 */
@Slf4j
@Service("openidUserDetailsService")
public class OpenIdUserDetailsService extends CustomUserDetailsService {
	@Setter
	private PasswordEncoder passwordEncoder;

	/**
	 * 调用/auth/token 调用这个方法校验
	 * @param uniqueId openId
	 * @author bonelf
	 * @date 2020-11-19 11:21
	 */
	@Override
	public UserDetails loadUserByUsername(String uniqueId) {
		User user;
		try {
			user = userService.getByUniqueId(uniqueId, UsernameType.openId);
		} catch (BonelfException be) {
			if ("404".equals(be.getCode())) {
				throw new UsernameNotFoundException((be.getErrorMessage()), be);
			} else {
				throw be;
			}
		}
		// 2020/11/19 错误和NPE处理
		log.info("load user by openId:{}", user.toString());
		// 如果为openId模式，从短信服务中获取验证码（动态密码）
		return new AuthUser(
				user.getUserId(),
				UsernameType.openId,
				user.getOpenId(),
				passwordEncoder.encode(OpenIdTokenGranter.PASSWORD),
				// OpenIdTokenGranter.PASSWORD,
				user.getEnabled(),
				user.getAccountNonExpired(),
				user.getCredentialsNonExpired(),
				user.getAccountNonLocked(),
				CollectionUtil.isEmpty(user.getRoles()) ?
						this.obtainGrantedAuthorities(user) :
						user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getCode())).collect(Collectors.toSet()));
	}
}
