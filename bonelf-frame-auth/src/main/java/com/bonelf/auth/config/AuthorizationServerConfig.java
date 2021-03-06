package com.bonelf.auth.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.bonelf.auth.core.exception.CustomWebResponseExceptionTranslator;
import com.bonelf.auth.core.oauth2.converter.CustomTokenEnhancer;
import com.bonelf.auth.core.oauth2.granter.mail.MailTokenGranter;
import com.bonelf.auth.core.oauth2.granter.mobile.MobileTokenGranter;
import com.bonelf.auth.core.oauth2.granter.openid.OpenIdTokenGranter;
import com.bonelf.frame.base.property.oauth2.Oauth2JwtProperties;
import com.bonelf.frame.base.service.IdUserDetailsService;
import com.bonelf.frame.core.auth.service.AuthUserService;
import com.bonelf.frame.web.security.converter.JwtWithUserInfoAccessTokenConverter;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * ???????????? ??????access_token?????????
 * </p>
 * @author bonelf
 * @since 2020/11/17 15:37
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private Oauth2JwtProperties oauth2JwtProperties;
	//@Autowired
	//private WebResponseExceptionTranslator<OAuth2Exception> customExceptionTranslator;
	@Autowired
	@Qualifier("authenticationManagerBean")
	private AuthenticationManager authenticationManager;
	@Qualifier("dataSource")
	@Autowired
	private DataSource dataSource;
	@Autowired
	@Qualifier("userDetailsService")
	private UserDetailsService userDetailsService;
	@Autowired
	private WxMaService wxMaService;
	@Autowired
	private AuthUserService userService;
	@Autowired(required = false)
	// @Qualifier("idUserDetailsService")
	private IdUserDetailsService idUserDetailsService;

	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
		// ?????????client????????????header???body???
		oauthServer.allowFormAuthenticationForClients();
		oauthServer.tokenKeyAccess("isAuthenticated()")
				.checkTokenAccess("permitAll()");
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		// ??????????????????????????????????????????????????????oauth_client_details???
		clients.jdbc(dataSource);
        /*
        ???????????????????????????????????????????????????
         */
		//int accessTokenValidity = ??;
		//accessTokenValidity = Math.max(accessTokenValidity, MIN_ACCESS_TOKEN_VALIDITY_SECS);
		//int refreshTokenValidity = refreshTokenValidityInSecondsForRememberMe;
		//refreshTokenValidity = Math.max(refreshTokenValidity, accessTokenValidity);
		//clients.inMemory()
		//		.withClient(clientId)
		//		.secret(passwordEncoder.encode(clientSecret))
		//		.scopes("web")
		//		.autoApprove(true)
		//		.authorizedGrantTypes("implicit","refresh_token", "password", "authorization_code")
		//		.accessTokenValiditySeconds(accessTokenValidity)
		//		.refreshTokenValiditySeconds(refreshTokenValidity)
		//		.and()
		//		.withClient(clientId2)
		//		.secret(passwordEncoder.encode(clientSecret2))
		//		.scopes("app")
		//		.authorities("ROLE_ADMIN")
		//		.autoApprove(true)
		//		.authorizedGrantTypes("client_credentials")
		//		.accessTokenValiditySeconds(3600)
		//		.refreshTokenValiditySeconds(tokenValidityInSecondsForRememberMe);
	}

	public static void main(String[] args) {
		//System.out.println(new BCryptPasswordEncoder().matches("app_secret","$2a$10$smMhxDIvYlaSAhba/BJekeDktJ/76LfkIfKezqJZg7tSxsej0RYPG"));
		System.out.println(new BCryptPasswordEncoder().encode("980826"));
		//System.out.println(new BCryptPasswordEncoder().encode("app_secret"));
		//System.out.println(new BCryptPasswordEncoder().encode("web_secret"));
		//System.out.println(new BCryptPasswordEncoder().encode("third_secret"));
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
		// ??????token???????????????????????????tokenServices?????????,?????????????????????????????????????????????TokenStore???TokenGranter???OAuth2RequestFactory
		endpoints.tokenStore(tokenStore())
				.authorizationCodeServices(authorizationCodeServices())
				.approvalStore(approvalStore())
				.exceptionTranslator(customExceptionTranslator())
				.tokenEnhancer(tokenEnhancerChain())
				.authenticationManager(authenticationManager)
				.userDetailsService(userDetailsService)
				//update by joe_chen add  granter
				.tokenGranter(tokenGranter(endpoints));

	}

	/**
	 * ?????????OAuth2???????????? ResourceConfig??????
	 * @return CustomWebResponseExceptionTranslator
	 */
	@Bean
	public WebResponseExceptionTranslator<OAuth2Exception> customExceptionTranslator() {
		return new CustomWebResponseExceptionTranslator();
	}

	/**
	 * ???????????????????????????
	 * @return JdbcApprovalStore
	 */
	@Bean
	public ApprovalStore approvalStore() {
		return new JdbcApprovalStore(dataSource);
	}

	/**
	 * ?????????????????????????????????code
	 * @return JdbcAuthorizationCodeServices
	 */
	@Bean
	protected AuthorizationCodeServices authorizationCodeServices() {
		// ??????????????????????????????????????????jdbc?????????oauth_code???
		return new JdbcAuthorizationCodeServices(dataSource);
	}

	/**
	 * token????????????
	 * @return JwtTokenStore
	 */
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	/**
	 * ?????????token
	 * @return tokenEnhancerChain
	 */
	@Bean
	public TokenEnhancerChain tokenEnhancerChain() {
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(new CustomTokenEnhancer(), accessTokenConverter()));
		return tokenEnhancerChain;
	}

	/**
	 * jwt token???????????????
	 * @return
	 */
	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setAccessTokenConverter(new JwtWithUserInfoAccessTokenConverter(idUserDetailsService));
		// 1:
		// converter.setSigningKey(oauth2JwtProperty.getSigningKey());
		// ?????? Cannot convert access token to JSON ???????????????NPE???verifier?????????????????????
		// converter.setVerifier(new RsaVerifier("---Begin--???---End---"));
		// 2:
		if (!StringUtils.hasText(oauth2JwtProperties.getKeystore())) {
			throw new RuntimeException("keystore is not set");
		}
		KeyPair keyPair = new KeyStoreKeyFactory(
				new ClassPathResource(oauth2JwtProperties.getKeystore()), oauth2JwtProperties.getPassword().toCharArray())
				.getKeyPair(oauth2JwtProperties.getAlias());
		converter.setKeyPair(keyPair);
		// 3:
		// converter.setVerifierKey(jwtVerifierKey);
		return converter;
	}

	/**
	 * ??????????????????granter,????????????????????????
	 * @param endpoints
	 * @author bonelf
	 */
	public TokenGranter tokenGranter(final AuthorizationServerEndpointsConfigurer endpoints) {
		List<TokenGranter> granters = Lists.newArrayList(endpoints.getTokenGranter());
		//??????granter ??????????????????
		MobileTokenGranter mobileTokenGranter = new MobileTokenGranter(
				authenticationManager,
				endpoints.getTokenServices(),
				endpoints.getClientDetailsService(),
				endpoints.getOAuth2RequestFactory(), userService);
		mobileTokenGranter.setReAuthIfNotFound(true);
		granters.add(mobileTokenGranter);

		MailTokenGranter mailTokenGranter = new MailTokenGranter(
				authenticationManager,
				endpoints.getTokenServices(),
				endpoints.getClientDetailsService(),
				endpoints.getOAuth2RequestFactory(), userService);
		mailTokenGranter.setReAuthIfNotFound(true);
		granters.add(mailTokenGranter);

		OpenIdTokenGranter openIdTokenGranter = new OpenIdTokenGranter(
				authenticationManager,
				endpoints.getTokenServices(),
				endpoints.getClientDetailsService(),
				endpoints.getOAuth2RequestFactory(),
				wxMaService,
				userService);
		openIdTokenGranter.setReAuthIfNotFound(true);
		granters.add(openIdTokenGranter);
		return new CompositeTokenGranter(granters);
	}

}