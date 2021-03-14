package com.bonelf.frame.base.property.oauth2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bonelf.oauth2")
public class Oauth2Properties {
	/**
	 * Oauth2JwtProperty
	 */
	private Oauth2JwtProperties jwt = new Oauth2JwtProperties();
	/**
	 * 不需要认证的api
	 */
	private String[] noAuthPath = new String[]{};
}
