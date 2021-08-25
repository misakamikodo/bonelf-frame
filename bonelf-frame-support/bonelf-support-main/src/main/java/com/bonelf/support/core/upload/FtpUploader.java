package com.bonelf.support.core.upload;

import cn.hutool.core.date.DateUtil;
import com.bonelf.support.core.upload.base.Uploader;
import com.bonelf.support.property.BonelfFtpProperties;
import com.bonelf.support.web.domain.vo.FileVO;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 本地文件上传
 * @author ccy
 * @date 2021/8/24 22:41
 */
@ConditionalOnProperty(prefix = "bonelf.upload", value = "type", havingValue = "ftp")
@Component
public class FtpUploader extends Uploader {
	@Autowired
	private BonelfFtpProperties bonelfFtpProperties;

	@Override
	public String upload(MultipartFile file) {
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
