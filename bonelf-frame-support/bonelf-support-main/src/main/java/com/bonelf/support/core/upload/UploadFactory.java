package com.bonelf.support.core.upload;

import com.bonelf.frame.base.util.SpringContextUtils;
import com.bonelf.support.core.upload.base.Uploader;
import com.bonelf.support.property.BonelfUploadProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 文件上传
 * @author ccy
 * @date 2021/8/24 22:40
 */
@Component
public class UploadFactory {
	@Autowired
	private BonelfUploadProperties bonelfUploadProperties;

	public Uploader getUploader() {
		switch (bonelfUploadProperties.getType()) {
			case local:
				return SpringContextUtils.getBean(LocalUploader.class);
			case minio:
				return SpringContextUtils.getBean(MinioUploader.class);
			case aliOss:
				return SpringContextUtils.getBean(AliOssUploader.class);
			case ftp:
				return SpringContextUtils.getBean(FtpUploader.class);
			default:
		}
		return null;
	}
}
