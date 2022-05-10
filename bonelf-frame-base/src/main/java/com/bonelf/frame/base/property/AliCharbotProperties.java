package com.bonelf.frame.base.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI机器人
 **/
@Data
@Component
@ConfigurationProperties(prefix = "third.ali.chatbot")
public class AliCharbotProperties {

	private String region = "cn-hangzhou";

	private String sysVersion = "2017-10-11";

	private String accessKeyId;

	private String accessSecret;

	private String product;

	private String popDomain;

	private String action;

	private String defaultInstanceId;
}
