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
	 * http://minio.bonelf.com
	 */
	private String endpoint = "http://192.168.31.60:9000";
	/**
	 * bucket
	 */
	private String bucket = "bonelf";

	/**
	 * 最大分片数
	 */
	private Integer maxPartNum = 10000;

	/**
	 * 最大分片大小
	 */
	private Long maxPartSize = 1024 * 1024 * 5L;

	/**
	 * 最大文件大小
	 */
	private Long maxMultipartPutObjectSize = 1024 * 1024 * 1024 * 5L;
}
