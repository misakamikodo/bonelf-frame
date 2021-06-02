/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.frame.cloud.config.security;

import com.bonelf.frame.base.property.oauth2.Oauth2Properties;
import com.bonelf.frame.cloud.security.constant.AuthFeignConstant;
import com.bonelf.frame.web.security.AuthExceptionEntryPoint;
import com.bonelf.frame.web.security.converter.JwtWithUserInfoAccessTokenConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * TODO 可以考虑abstract 如果后续扩展
 * </p>
 * @author bonelf
 * @since 2020/11/19 17:36
 */
@EnableResourceServer
@Configuration
public class CloudResourceServerConfig extends ResourceServerConfigurerAdapter {
	@Autowired
	private Oauth2Properties oauth2Properties;
	@Autowired(required = false)
	@Qualifier("idUserDetailsService")
	private UserDetailsService userDetailsService;

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
				// .accessDeniedHandler(new AuthExceptionHandler())
				// .authenticationEntryPoint(new AuthExceptionEntryPoint())
				.and()
				.csrf().disable()
				//.antMatcher("/login").anonymous()
				//.and()
				.authorizeRequests()
				// websocket 放权（握手是独立验证）
				.antMatchers("/wst").permitAll()
				// Feign请求全部不需要认证
				.requestMatchers(request -> {
					String head = request.getHeader(AuthFeignConstant.AUTH_HEADER);
					return head != null && head.startsWith(AuthFeignConstant.FEIGN_REQ_FLAG_PREFIX);
				}).permitAll()
				// 测试全部放权用
				// .requestMatchers(request -> true).permitAll()
				.mvcMatchers(oauth2Properties.getNoAuthPath()).permitAll()
				// 测试全部放权用
				// .mvcMatchers("/*").permitAll()
				.anyRequest().authenticated()
				// .and()
				// .formLogin().loginPage("/login")
				// .failureHandler(null)
		;
	}

	@Bean
	public AuthenticationEntryPoint authExceptionEntryPoint() {
		return new AuthExceptionEntryPoint();
	}

	@Bean
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