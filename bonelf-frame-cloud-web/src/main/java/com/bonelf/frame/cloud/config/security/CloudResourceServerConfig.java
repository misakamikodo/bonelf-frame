/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.frame.cloud.config.security;

import com.bonelf.frame.base.property.oauth2.Oauth2Properties;
import com.bonelf.frame.cloud.constant.AuthFeignConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * <p>
 * 令牌认证 拿到access_token后调用接口的配置
 * </p>
 * @author bonelf
 * @since 2020/11/19 17:36
 */
@EnableResourceServer
@Configuration
public class CloudResourceServerConfig extends ResourceServerConfigurerAdapter {
	@Autowired
	private Oauth2Properties oauth2Properties;

	@Override
	public void configure(HttpSecurity http) throws Exception {
		//super.configure(http);
		http.exceptionHandling()
				//.accessDeniedHandler(authExceptionHandler())
				.and()
				.csrf().disable()
				//.antMatcher("/login").anonymous()
				//.and()
				.authorizeRequests()
				// Feign请求全部不需要认证
				.requestMatchers(request -> {
					String head = request.getHeader(AuthFeignConstant.AUTH_HEADER);
					return head != null && head.startsWith(AuthFeignConstant.FEIGN_REQ_FLAG_PREFIX);
				}).permitAll()
				.mvcMatchers(oauth2Properties.getNoAuthPath()).permitAll()
				// FIXME: 2020/12/1
				.mvcMatchers("/*").permitAll()
				.anyRequest().authenticated();
	}
}