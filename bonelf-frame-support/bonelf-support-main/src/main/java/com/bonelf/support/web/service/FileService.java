package com.bonelf.support.web.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

	String uploadOssFile(MultipartFile file) throws IOException;

	String uploadFile(MultipartFile file) throws IOException;
}
