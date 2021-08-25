package com.bonelf.support.core.upload;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.bonelf.frame.base.property.BonelfProperties;
import com.bonelf.support.core.upload.base.Uploader;
import com.bonelf.support.property.OssProperties;
import com.bonelf.support.web.domain.vo.FileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 本地文件上传
 * @author ccy
 * @date 2021/8/24 22:41
 */
@ConditionalOnProperty(prefix = "bonelf.upload", value = "type", havingValue = "aliOss")
@Component
public class AliOssUploader extends Uploader {
	@Autowired
	private OssProperties ossProperties;
	@Autowired
	private BonelfProperties bonelfProperties;
	@Value("${spring.application.name:support}")
	private String appName;
	@Value("${server.servlet.context-path:/bonelf}")
	private String ctxPath;

	@Override
	public String upload(MultipartFile file) throws IOException {
		String fileStr = getFileNameForBucketUpload(file);
		OSS ossClient = new OSSClientBuilder().build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(), ossProperties.getAccessSecret());
		ossClient.putObject(ossProperties.getBucketName(), fileStr, file.getInputStream());
		ossClient.shutdown();
		return ossProperties.getBucketUrl() + "/" + fileStr;
	}

	@Override
	public FileVO getChunks(String md5) {
		throw new UnsupportedOperationException("暂不支持分片上传");
	}

	@Override
	public FileVO newMultipart(Integer totalChunkCounts, String md5, Long size, String fileName) {
		throw new UnsupportedOperationException("暂不支持分片上传");
	}

	@Override
	public FileVO getMultipartUrl(String uuid, String uploadId, Long size, Integer chunkNumber) {
		throw new UnsupportedOperationException("暂不支持分片上传");
	}

	@Override
	public FileVO updateChunk(String uuid, Integer chunkNumber, String etag) {
		throw new UnsupportedOperationException("暂不支持分片上传");
	}

	@Override
	public FileVO completeMultipart(String uuid, String uploadId, String objectName, Long size) {
		throw new UnsupportedOperationException("暂不支持分片上传");
	}
}
