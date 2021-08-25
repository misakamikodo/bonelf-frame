/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.support.config;

import com.bonelf.support.core.upload.CustomerMinioClient;
import com.bonelf.support.property.BonelfMinioProperties;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * minio
 * @author bonelf
 * @date 2021/6/7 16:01
 */
@ConditionalOnProperty(prefix = "bonelf.upload", value = "type", havingValue = "minio")
@Configuration
public class MinioConfig {
	@Autowired
	private BonelfMinioProperties bonelfMinioProperties;

	/**
	 * 获取 MinioClient
	 * @return
	 */
	@Bean
	public CustomerMinioClient minioClient() {
		return new CustomerMinioClient(new MinioClient
				.Builder()
				.endpoint(bonelfMinioProperties.getEndpoint())
				.credentials(bonelfMinioProperties.getAccessKey(), bonelfMinioProperties.getSecretKey())
				.build());
	}
}
