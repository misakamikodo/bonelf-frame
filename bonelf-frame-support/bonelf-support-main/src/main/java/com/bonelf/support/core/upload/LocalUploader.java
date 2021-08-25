package com.bonelf.support.core.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.bonelf.frame.base.property.BonelfProperties;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.support.core.upload.base.Uploader;
import com.bonelf.support.web.domain.vo.FileVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 本地文件上传
 * @author bonelf
 * @date 2021/8/24 22:41
 */
@ConditionalOnProperty(prefix = "bonelf.upload", value = "type", havingValue = "local")
@Component
@Slf4j
public class LocalUploader extends Uploader {
	@Autowired
	private BonelfProperties bonelfProperties;
	@Value("${spring.application.name:support}")
	private String appName;
	@Value("${server.servlet.context-path:/bonelf}")
	private String ctxPath;

	/**
	 * /静态文件路由/文件类型路由/上传时间/原始文件名（中文改写为unicode去掉%组合 可考虑转MD5）
	 * @param file
	 * @return
	 */
	@Override
	public String upload(MultipartFile file) {
		String staticPathName = "static";
		String typeDirName;
		if (isImage(file)) {
			typeDirName = "image";
		} else {
			typeDirName = "resource";
		}
		//创建目录
		String dateDirName = DateUtil.today();
		String dirName = staticPathName + "/" + typeDirName + "/" + dateDirName;
		File dir = new File(dirName);
		if (!dir.exists()) {
			mkdir(dir);
			//boolean f = dir.mkdir();
			//if (!f) {
			//	throw new BonelfException("创建文件目录失败");
			//}
		}
		//名称合法化
		String validFileNameTmp = StrUtil.nullToDefault(file.getOriginalFilename(), String.valueOf(System.currentTimeMillis()));
		try {
			validFileNameTmp = URLEncoder.encode(validFileNameTmp, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.warn("文件上传编码失败name:{}", validFileNameTmp);
			e.printStackTrace();
		}
		String validFileName = validFileNameTmp.replace("%", "");
		//创建文件
		File targetFile = new File(dirName + "/" + validFileName);
		if (!targetFile.exists()) {
			createFile(targetFile);
		} else {
			validFileName = RandomUtil.randomString(5) + validFileName;
			targetFile = new File(dirName + "/" + validFileName);
			if (!targetFile.exists()) {
				createFile(targetFile);
			} else {
				throw new BonelfException("系统错误，请重新上传");
			}
		}
		//上传文件
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(targetFile);
			IOUtils.copy(file.getInputStream(), fileOutputStream);
			log.info("------>>>>>>uploaded a file successfully!<<<<<<------");
		} catch (IOException e) {
			e.printStackTrace();
			throw new BonelfException("文件上传失败");
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					log.warn("输出流关闭失败", e);
				}
			}
		}
		return bonelfProperties.getBaseUrl() + ctxPath + "/" + appName + "/" + typeDirName + "/" + dateDirName + "/" + validFileName;
	}

	/**
	 * 一级一级创建目录，理论上不用这样
	 * @param dir
	 */
	protected static void mkdir(File dir) {
		while (dir.getParentFile() != null && !dir.getParentFile().exists()) {
			mkdir(dir.getParentFile());
		}
		if (!dir.exists()) {
			boolean f = dir.mkdir();
			if (!f) {
				throw new BonelfException("创建文件目录失败");
			}
		}
	}

	protected void createFile(File targetFile) {
		boolean f;
		try {
			f = targetFile.createNewFile();
		} catch (IOException e) {
			throw new BonelfException("创建文件失败");
		}
		if (!f) {
			throw new BonelfException("创建文件失败");
		}
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
