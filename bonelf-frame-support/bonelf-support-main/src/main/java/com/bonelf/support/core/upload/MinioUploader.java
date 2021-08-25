package com.bonelf.support.core.upload;

import cn.hutool.core.util.StrUtil;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.support.core.upload.base.Uploader;
import com.bonelf.support.property.BonelfMinioProperties;
import com.bonelf.support.web.domain.dto.FileDTO;
import com.bonelf.support.web.domain.vo.FileVO;
import com.google.common.collect.HashMultimap;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 本地文件上传
 * 分片上传：
 * 初始化 返回 uploadId，分片上传地址
 * 前端根据上传地址上传对应分片
 * 完成上传后 调用完成上传
 * @author bonelf
 * @date 2021/8/24 22:41
 */
@ConditionalOnProperty(prefix = "bonelf.upload", value = "type", havingValue = "minio")
@Component
@Slf4j
public class MinioUploader extends Uploader {
	@Autowired
	private BonelfMinioProperties bonelfMinioProperties;
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;
	@Autowired(required = false)
	private CustomerMinioClient minioClient;

	@Override
	public String upload(MultipartFile file) throws IOException {
		if (minioClient != null) {
			// 文件名
			String fileStr = getFileNameForBucketUpload(file);
			// 开始上传
			PutObjectArgs args = PutObjectArgs.builder()
					.bucket(bonelfMinioProperties.getBucket())
					.stream(file.getInputStream(), file.getSize(), PutObjectArgs.MIN_MULTIPART_SIZE)
					.contentType(file.getContentType())
					.object(fileStr)
					.build();
			try {
				minioClient.putObject(args);
			} catch (Exception e) {
				throw dealException(e);
			}
			// 想要预览还要带上token ?token={minio jwt str}
			return bonelfMinioProperties.getEndpoint() + "/" + bonelfMinioProperties.getBucket() + "/" + fileStr;
		}
		return null;
	}

	/**
	 * 没上传完成后调用获取下一个分片上传地址
	 * @param md5
	 * @return
	 */
	@Override
	public FileVO getChunks(String md5) {
		// 秒传功能需要保存已传的文件md5，也可记录在数据库中
		FileDTO fileChunk = (FileDTO)redisTemplate.opsForValue()
				.get(String.format(CacheConstant.FILE_MUILT_UPLOAD_MD5, md5));
		String uuid = null, uploadId = null, chunks = null;
		boolean uploaded = false;
		if (fileChunk != null) {
			uuid = fileChunk.getUuid();
			uploaded = fileChunk.getUploaded();
			uploadId = fileChunk.getUploadId();

			// 判断是否存在对象
			Iterable<Result<Item>> list = minioClient.listObjects(ListObjectsArgs.builder()
					.bucket(bonelfMinioProperties.getBucket()).prefix(uuid).recursive(false).build());
			// 如果为空
			if (list.iterator().hasNext()) {
				uploaded = true;
				fileChunk.setUploaded(true);
				redisTemplate.opsForValue()
						.set(String.format(CacheConstant.FILE_MUILT_UPLOAD_MD5, md5), fileChunk, CacheConstant.FILE_MUILT_UPLOAD_EXPIRE);
			} else {
				uploaded = false;
				fileChunk.setUploaded(false);
				redisTemplate.opsForValue()
						.set(String.format(CacheConstant.FILE_MUILT_UPLOAD_MD5, md5), fileChunk, CacheConstant.FILE_MUILT_UPLOAD_EXPIRE);
				// 下面可以一次性把所有分片上传url给前端
				// ListPartsResponse parts = null;
				// try {
				// 	parts = minioClient.listParts(
				// 			bonelfMinioProperties.getBucket(), null, uuid, null, null, uploadId, null, null);
				// } catch (Exception e) {
				// 	throw dealException(e);
				// }
				// StringJoiner chunksJoiner = new StringJoiner(",");
				// StringJoiner etagJoiner = new StringJoiner(",");
				// if (parts.result().partList().size() > 0) {
				// 	for (Part part : parts.result().partList()) {
				// 		chunksJoiner.add(String.valueOf(part.partNumber()));
				// 		etagJoiner.add(part.etag());
				// 	}
				// }
			}
		}
		FileVO result = new FileVO();
		result.setUuid(uuid);
		result.setUploaded(uploaded);
		result.setUploadId(uploadId);
		result.setChunks(chunks);
		return result;
	}

	@Override
	public FileVO newMultipart(Integer totalChunkCounts, String md5, Long size, String fileName) {
		if (totalChunkCounts > bonelfMinioProperties.getMaxPartNum() || totalChunkCounts <= 0) {
			throw new BonelfException("分片数量不得大于" + bonelfMinioProperties.getMaxPartNum());
		}
		if (size > bonelfMinioProperties.getMaxMultipartPutObjectSize() || size < 0) {
			throw new BonelfException("分片大小不得大于" + (bonelfMinioProperties.getMaxMultipartPutObjectSize() / 1024 / 1024) + "MB");
		}
		String objectName = this.getFileNameForBucketUpload(fileName);
		FileVO result = new FileVO();
		try {
			String uploadId = minioClient.initMultiPartUpload(bonelfMinioProperties.getBucket(),
					null, fileName, null, null);
			result.setUploadId(uploadId);
		} catch (Exception e) {
			throw dealException(e);
		}
		result.setObjectName(fileName);
		result.setUuid(UUID.randomUUID().toString().replace("-", ""));
		return result;
	}

	/**
	 * 获取minio上传地址
	 * @param objectName
	 * @param uploadId
	 * @param size
	 * @param chunkNumber
	 * @return
	 */
	@Override
	public FileVO getMultipartUrl(String objectName, String uploadId, Long size, Integer chunkNumber) {
		// FileDTO fileChunk = (FileDTO)redisTemplate.opsForValue()
		// 		.get(String.format(CacheConstant.FILE_MUILT_UPLOAD_MD5, md5));
		Map<String, String> reqParams = new HashMap<>();
		reqParams.put("uploadId", uploadId);
		String uploadUrl;
		try {
			reqParams.put("partNumber", String.valueOf(chunkNumber));
			uploadUrl = minioClient.getPresignedObjectUrl(
					GetPresignedObjectUrlArgs.builder()
							.method(Method.PUT)
							.bucket(bonelfMinioProperties.getBucket())
							.object(objectName)
							.expiry(1, TimeUnit.DAYS)
							.extraQueryParams(reqParams)
							.build());
		} catch (Exception e) {
			throw dealException(e);
		}
		FileVO result = new FileVO();
		result.setUrl(uploadUrl);
		return result;
	}

	@Override
	public FileVO completeMultipart(String uuid, String uploadId, String objectName, Long size) {
		try {
			Part[] parts = new Part[bonelfMinioProperties.getMaxPartNum()];
			ListPartsResponse partResult = minioClient.listParts(bonelfMinioProperties.getBucket(),
					null, objectName, 1000, 0, uploadId, null, null);
			int partNumber = 1;
			for (Part part : partResult.result().partList()) {
				parts[partNumber - 1] = new Part(partNumber, part.etag());
				partNumber++;
			}
			minioClient.completeMultipartUpload(bonelfMinioProperties.getBucket(), null, objectName, uploadId, parts, null, null);
		} catch (Exception e) {
			dealException(e);
		}
		FileVO result = new FileVO();
		result.setUrl(bonelfMinioProperties.getEndpoint() + "/" + bonelfMinioProperties.getBucket() + "/" + objectName);
		return result;
	}

	@Override
	public FileVO updateChunk(String uuid, Integer chunkNumber, String etag) {
		throw new UnsupportedOperationException("待开发功能");
	}

	private BonelfException dealException(Exception e) {
		e.printStackTrace();
		return new BonelfException(e);
	}

}
