package com.bonelf.support.core.upload.base;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.bonelf.support.web.domain.vo.FileVO;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

/**
 * 文件上传接口
 * @author bonelf
 * @date 2021/8/24 22:42
 */
@Slf4j
public abstract class Uploader {
	/**
	 * 单文件上传
	 * @param file
	 * @return
	 */
	public abstract String upload(MultipartFile file) throws IOException;

	/*================断点上传、秒传=================*/

	/**
	 * 获取已处理的分片
	 * @param md5
	 * @return
	 */
	public abstract FileVO getChunks(String md5);

	public abstract FileVO newMultipart(Integer totalChunkCounts, String md5, Long size, String fileName);

	public abstract FileVO getMultipartUrl(String uuid, String uploadId, Long size, Integer chunkNumber);

	public abstract FileVO updateChunk(String uuid, Integer chunkNumber, String etag);

	public abstract FileVO completeMultipart(String uuid, String uploadId, String objectName, Long size);


	/**
	 * 创建文件名
	 * @param file
	 * @return
	 */
	protected String getFileNameForBucketUpload(MultipartFile file) {
		String fileStr;
		try {
			fileStr = DateUtil.today() + "/" + getFileName(file);
		} catch (UnsupportedEncodingException e) {
			log.warn("文件上传编码失败name:{}", file.getOriginalFilename());
			e.printStackTrace();
			fileStr = DateUtil.today() + "/" + file.getOriginalFilename();
		}
		return fileStr;
	}

	protected String getFileNameForBucketUpload(String fileName) {
		String fileStr;
		try {
			fileStr = DateUtil.today() + "/" + getFileName(fileName);
		} catch (UnsupportedEncodingException e) {
			log.warn("文件上传编码失败name:{}", fileName);
			e.printStackTrace();
			fileStr = DateUtil.today() + "/" + fileName;
		}
		return fileStr;
	}

	/**
	 * 文件名格式化
	 * @param file
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	protected String getFileName(MultipartFile file) throws UnsupportedEncodingException {
		return URLEncoder.encode(StrUtil.nullToEmpty(file.getOriginalFilename()), "UTF-8").replace("%", "");
	}

	protected String getFileName(String fileName) throws UnsupportedEncodingException {
		return URLEncoder.encode(StrUtil.nullToEmpty(fileName), "UTF-8").replace("%", "");
	}

	/**
	 * 判断是否图片
	 * @param file
	 * @return
	 */
	protected boolean isImage(MultipartFile file) {
		byte[] fileBytes = null;
		try {
			fileBytes = file.getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (fileBytes != null) {
			Collection<?> mimeType;
			mimeType = MimeUtil.getMimeTypes(fileBytes);
			if (mimeType.size() < 1) {
				// 尝试使用文件头magic探测
				MagicMimeMimeDetector magicMimeDetector = new MagicMimeMimeDetector();
				mimeType = magicMimeDetector.getMimeTypes(fileBytes);
			}
			if (mimeType.size() > 0) {
				String fileType = mimeType.iterator().next().toString();
				if (fileType != null) {
					String type = fileType.split("/")[0];
					return "image".equals(type);
				}
			}
		} else {
			if (file.getContentType() != null) {
				String type = file.getContentType().split("/")[0];
				return "image".equals(type);
			}
		}
		return false;
	}
}
