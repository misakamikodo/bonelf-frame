package com.bonelf.support.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * minio
 * @author bonelf
 * @date 2021/6/7 15:51
 */
@Data
@Component
@ConfigurationProperties(prefix = "bonelf.upload.minio")
public class BonelfMinioProperties {
	/**
	 * id
	 */
	private String accessKey = "admin";
	/**
	 * 秘钥
	 */
	private String secretKey = "bonelf@123";
	/**
	 * 节点
	 */
	private String endpoint = "http://minio.bonelf.com";
	/**
	 * bucket
	 */
	private String bucket = "bonelf";
}
