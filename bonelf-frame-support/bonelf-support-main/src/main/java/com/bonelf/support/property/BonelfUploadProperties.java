package com.bonelf.support.property;

import com.bonelf.support.constant.UploadTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bonelf.upload")
public class BonelfUploadProperties {
	/**
	 * 项目网址
	 */
	private UploadTypeEnum type = UploadTypeEnum.local;
}
