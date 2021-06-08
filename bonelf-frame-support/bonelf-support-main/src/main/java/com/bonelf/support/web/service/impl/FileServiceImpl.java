package com.bonelf.support.web.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.bonelf.frame.base.property.BonelfProperties;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.support.property.BonelfFtpProperties;
import com.bonelf.support.property.BonelfMinioProperties;
import com.bonelf.support.property.OssProperties;
import com.bonelf.support.web.service.FileService;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
	@Autowired
	private OssProperties ossProperties;
	@Autowired
	private BonelfProperties bonelfProperties;
	@Autowired
	private BonelfMinioProperties bonelfMinioProperties;
	@Autowired
	private BonelfFtpProperties bonelfFtpProperties;
	@Autowired(required = false)
	private MinioClient minioClient;
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
	public String uploadFile(MultipartFile file) {
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


	@Override
	public String uploadOssFile(MultipartFile file) throws IOException {
		String fileStr = getFileNameForBucketUpload(file);
		OSS ossClient = new OSSClientBuilder().build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(), ossProperties.getAccessSecret());
		ossClient.putObject(ossProperties.getBucketName(), fileStr, file.getInputStream());
		ossClient.shutdown();
		return ossProperties.getBucketUrl() + "/" + fileStr;
	}

	@Override
	public String uploadMinioFile(MultipartFile file) throws IOException {
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
			} catch (ErrorResponseException e) {
				e.printStackTrace();
			} catch (InsufficientDataException e) {
				e.printStackTrace();
			} catch (InternalException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (InvalidResponseException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (ServerException e) {
				e.printStackTrace();
			} catch (XmlParserException e) {
				e.printStackTrace();
			}
			// 想要预览还要带上token ?token={minio jwt str}
			return bonelfMinioProperties.getEndpoint() + "/" + bonelfMinioProperties.getBucket() + "/" + fileStr;
		}
		return null;
	}

	@Override
	public String uploadFtpFile(MultipartFile file) {
		try {
			// 创建FTPClient对象
			FTPClient f = new FTPClient();
			// 连接端口使用21
			f.connect(bonelfFtpProperties.getIp(), bonelfFtpProperties.getPort());
			// 给定用户名和密码完成
			f.login(bonelfFtpProperties.getUsername(), bonelfFtpProperties.getPassword());
			// 指定上传文件的保存目录
			String dateDir = DateUtil.today();
			String dir = bonelfFtpProperties.getBucket() + "/" + dateDir;
			if (!f.changeWorkingDirectory(dir)) {
				//如果目录不存在创建目录
				String[] dirs = new String[]{bonelfFtpProperties.getBucket(), dateDir};
				String tempPath = "";
				for (String d : dirs) {
					tempPath += "/" + d;
					if (!f.changeWorkingDirectory(tempPath)) {
						if (f.makeDirectory(tempPath)) {
							f.changeWorkingDirectory(tempPath);
						}
					}
				}
			}
			// 开启字节流传输
			f.setFileType(FTPClient.BINARY_FILE_TYPE);
			// 文件上传
			String fileName = getFileName(file);
			f.storeFile(fileName, file.getInputStream());
			// 退出登录
			f.logout();
			// 想要预览还要带上token ?token={minio jwt str}
			return bonelfFtpProperties.getEndpoint() + "/" + dir + "/" + fileName;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 创建文件名
	 * @param file
	 * @return
	 */
	private String getFileNameForBucketUpload(MultipartFile file) {
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

	/**
	 * 文件名格式化
	 * @param file
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getFileName(MultipartFile file) throws UnsupportedEncodingException {
		return URLEncoder.encode(StrUtil.nullToEmpty(file.getOriginalFilename()), "UTF-8").replace("%", "");
	}

	/**
	 * 一级一级创建目录，理论上不用这样
	 * @param dir
	 */
	private static void mkdir(File dir) {
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

	/**
	 * 判断是否图片
	 * @param file
	 * @return
	 */
	private boolean isImage(MultipartFile file) {
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

	private void createFile(File targetFile) {
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
}
