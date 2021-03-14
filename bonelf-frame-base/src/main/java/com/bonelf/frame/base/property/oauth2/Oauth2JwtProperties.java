package com.bonelf.frame.base.property.oauth2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bonelf.oauth2.jwt")
public class Oauth2JwtProperties {
	/**
	 * jwt签名
	 * use keystore instead
	 */
	@Deprecated
	private String signingKey = "123456";
	/**
	 * classpath 密钥文件
	 */
	private String keystore = "/tls/example.p12";
	/**
	 * 密码
	 */
	private String password = "=bonelf=";
	/**
	 * 别名
	 */
	private String alias = "bonelf";
}
