package com.bonelf.frame.base.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "bonelf")
public class BonelfProperties {
	/**
	 * 项目网址
	 * minio、oss替换为服务的地址
	 */
	private String baseUrl = "http://127.0.0.1:9999";

	private String appName = "bonelf经验总结项目";

	@Deprecated
	private List<String> permitPath = new ArrayList<>();
}
