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

	private String verifierKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCIRXo70fk4FHbq5AfNQwL90nLtSWghoz+GflRPelvBHmSfX" +
			"IDbdiOXvek9l2ZdkduMhLNWoj3hVR4UrDgu9K1vmew+G/bLLhllU99a/I8dddPyN8QvZloc/DafS5/JTSl28249wQ/OmJdJ08gAGGIocsB" +
			"95GrOR8kEP/0Ia5VhpYY1o27564Im27+FRuWVHwfB8ruLOlFQ49B48tVDNqKhGTDEOdWW4K5WiI1M2Y+2fi9rBVF+Fm5pHcsqHKpjNWBYvq" +
			"MGF6tZBWB5ytE7Old//3GEt07NqhQyrSNw1mqgSHTE8G1NMGnH0R6Ke7IdE6fd8FPPrQw58vSdikzNxW+P onhonest-oauth2-rsa2048";
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
