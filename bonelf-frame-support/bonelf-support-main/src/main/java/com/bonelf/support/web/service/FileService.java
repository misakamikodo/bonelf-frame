package com.bonelf.support.web.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

	/**
	 * 上传文件到本地
	 * @param file
	 * @return
	 * @throws IOException
	 */
	String uploadFile(MultipartFile file) throws IOException;

	/**
	 * 上传文件到阿里OSS
	 * @param file
	 * @return
	 * @throws IOException
	 */
	String uploadOssFile(MultipartFile file) throws IOException;

	/**
	 * 上传文件到Minio
	 * @param file
	 * @return
	 * @throws IOException
	 */
	String uploadMinioFile(MultipartFile file) throws IOException;


	/**
	 * 上传文件到Ftp
	 * @param file
	 * @return
	 * @throws IOException
	 */
	String uploadFtpFile(MultipartFile file);
}
