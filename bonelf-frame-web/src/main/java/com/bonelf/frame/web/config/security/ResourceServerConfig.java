/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.frame.web.config.security;

import cn.hutool.core.util.ArrayUtil;
import com.bonelf.frame.base.property.oauth2.Oauth2Properties;
import com.bonelf.frame.base.service.IdUserDetailsService;
import com.bonelf.frame.web.security.AuthExceptionEntryPoint;
import com.bonelf.frame.web.security.converter.JwtWithUserInfoAccessTokenConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;

import java.security.KeyPair;

/**
 * <p>
 * 令牌认证 拿到access_token后调用接口的配置
 * </p>
 * @author bonelf
 * @since 2020/11/19 17:36
 */
@ConditionalOnMissingBean(ResourceServerConfigurerAdapter.class)
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
	@Autowired
	private Oauth2Properties oauth2Properties;
	@Autowired(required = false)
	private IdUserDetailsService userDetailsService;

	@Override
	public void configure(ResourceServerSecurityConfigurer resourceServerSecurityConfigurer) {
		resourceServerSecurityConfigurer
				.tokenStore(tokenStore())
				.authenticationEntryPoint(authExceptionEntryPoint())
				.resourceId("WEBS");
	}

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
				// websocket 放权（握手是独立验证）
				.antMatchers("/wst").permitAll()
				.antMatchers(ArrayUtil.addAll(oauth2Properties.getPermitPath(), new String[]{})).permitAll()
				// .mvcMatchers("/*").permitAll()
				.anyRequest().authenticated();
	}

	//@Override
	//public void configure(HttpSecurity http) throws Exception {
	//	http.csrf()
	//        .disable()
	//        .headers()
	//        .frameOptions()
	//        .disable()
	//        .and()
	//        .sessionManagement()
	//        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	//        .and()
	//        .authorizeRequests()
	//        .antMatchers("/oauth/token_key").permitAll();
	//}

	//@Bean
	//public AuthExceptionHandler authExceptionHandler() {
	//	return new AuthExceptionHandler();
	//}

	@Bean
	public AuthenticationEntryPoint authExceptionEntryPoint() {
		return new AuthExceptionEntryPoint();
	}

	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setAccessTokenConverter(new JwtWithUserInfoAccessTokenConverter(userDetailsService));
		//1:
		//converter.setSigningKey(oauth2Property.getJwt().getSigningKey());
		//出现 Cannot convert access token to JSON （实际上为NPE，verifier为空）考虑设置
		//converter.setVerifier(new RsaVerifier("---Begin--???---End---"));
		//2:
		if (!StringUtils.hasText(oauth2Properties.getJwt().getKeystore())) {
			throw new RuntimeException("keystore is not set");
		}
		KeyPair keyPair = new KeyStoreKeyFactory(
				new ClassPathResource(oauth2Properties.getJwt().getKeystore()), oauth2Properties.getJwt().getPassword().toCharArray())
				.getKeyPair(oauth2Properties.getJwt().getAlias());
		converter.setKeyPair(keyPair);
		return converter;
	}
}