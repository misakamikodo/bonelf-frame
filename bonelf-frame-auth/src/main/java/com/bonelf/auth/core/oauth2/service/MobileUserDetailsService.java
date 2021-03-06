/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.auth.core.oauth2.service;

import cn.hutool.core.collection.CollectionUtil;
import com.bonelf.auth.service.SupportService;
import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
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
 * 手机验证码登录校验
 * 配置注入 {@link com.bonelf.auth.config.WebServerSecurityConfig}
 * @author bonelf
 */
@Slf4j
@Service("mobileUserDetailsService")
public class MobileUserDetailsService extends CustomUserDetailsService {
	@Autowired
	protected SupportService supportService;
	@Setter
	protected PasswordEncoder passwordEncoder;

	/**
	 * 调用/auth/token 调用这个方法校验
	 * @param uniqueId phone
	 * @author bonelf
	 * @date 2020-11-19 11:21
	 */
	@Override
	public UserDetails loadUserByUsername(String uniqueId) {
		User user;
		try {
			user = userService.getByUniqueId(uniqueId, UsernameType.phone);
		} catch (BonelfException be) {
			if ("404".equals(be.getCode())) {
				throw new UsernameNotFoundException((be.getErrorMessage()), be);
			} else {
				throw be;
			}
		}
		user.setVerifyCode(supportService.getVerifyPhone(user.getPhone(), VerifyCodeTypeEnum.LOGIN));
		log.debug("load user by mobile:{}", user.toString());
		// 如果为mobile模式，从短信服务中获取验证码（动态密码） 其实可以去了加密
		//String credentials = userClient.getSmsCode(uniqueId, "LOGIN");
		String credentials = passwordEncoder.encode(user.getVerifyCode());
		//String credentials = user.getVerifyCode();
		// String credentials = new BCryptPasswordEncoder().encode(user.getVerifyCode());
		return new AuthUser(
				user.getUserId(),
				UsernameType.phone,
				user.getPhone(),
				credentials,
				user.getEnabled(),
				user.getAccountNonExpired(),
				user.getCredentialsNonExpired(),
				user.getAccountNonLocked(),
				CollectionUtil.isEmpty(user.getRoles()) ?
						this.obtainGrantedAuthorities(user) :
						user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getCode())).collect(Collectors.toSet()));
	}
}
