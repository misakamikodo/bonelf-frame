package com.bonelf.support.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ali.oss")
public class OssProperties {
	/**
	 * id
	 */
	private String accessKeyId;
	/**
	 * 秘钥
	 */
	private String accessSecret;
	/**
	 * 节点
	 */
	private String endpoint;
	/**
	 * 库名
	 */
	private String bucketName;
	/**
	 * url
	 */
	private String bucketUrl;
}
