package com.bonelf.support.web.controller;

import com.bonelf.frame.core.domain.Result;
import com.bonelf.support.core.upload.UploadFactory;
import com.bonelf.support.web.domain.dto.FileDTO;
import com.bonelf.support.web.domain.vo.FileVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 第三方和公用接口
 * </p>
 * @author bonelf
 * @since 2020/10/30 9:29
 */
@RestController
@RequestMapping("/upload")
@Slf4j
@Api(tags = "上传接口")
public class UploadController {
	@Autowired
	private UploadFactory uploadFactory;

	@ApiOperation("上传文件到服务器")
	@RequestMapping(value = "/v1/file", method = RequestMethod.POST)
	public Result<?> uploadFile(@RequestParam MultipartFile file) throws IOException {
		//上传文件大小为1000条数据
		if (file.getSize() > 1024 * 1024 * 10) {
			log.error("upload | 上传失败: 文件大小超过10M，文件大小为：{}", file.getSize());
			return Result.error("上传失败: 文件大小不能超过10M!");
		}
		Map<String, Object> resp = new HashMap<>(1);
		resp.put("url", uploadFactory.getUploader().upload(file));
		return Result.ok(resp);
	}

	/*============================minio 断点上传、秒传==============================*/

	/**
	 * 获取已经成功的分片
	 * @param md5
	 * @return uploadId、uuid、uploaded、chunks
	 */
	@GetMapping("getChunks")
	public Result<FileVO> getChunks(@RequestParam String md5){
		return Result.ok(uploadFactory.getUploader().getChunks(md5));
	}

	/**
	 * 新分片上擦黄
	 * @param file totalChunkCounts、uniqueIdentifier、size、name
	 * @return uploadId、uuid
	 */
	@PostMapping("newMultipart")
	public Result<FileVO> newMultipart(@RequestBody FileDTO file){
		return Result.ok(uploadFactory.getUploader().newMultipart(
				file.getTotalChunkCounts(), file.getMd5(), file.getSize(), file.getFileName()
		));
	}

	/**
	 * 获取分片上传url
	 * uuid、uploadId、size、chunkNumber
	 * @return url
	 */
	@GetMapping("getMultipartUrl")
	public Result<FileVO> getMultipartUrl(FileDTO file){
		return Result.ok(uploadFactory.getUploader().getMultipartUrl(
				file.getObjectName(), file.getUploadId(), file.getSize(), file.getChunkNumber()
		));
	}

	/**
	 * 更新数据库 目前测试用
	 * @return
	 */
	@PostMapping("updateChunk")
	public Result<FileVO> updateChunk(@RequestBody FileDTO file){
		return Result.ok(uploadFactory.getUploader().updateChunk(
				file.getUuid(), file.getChunkNumber(), file.getEtag()
		));
	}

	@PostMapping("completeMultipart")
	public Result<FileVO> completeMultipart(@RequestBody FileDTO file){
		return Result.ok(uploadFactory.getUploader().completeMultipart(
				file.getUuid(), file.getUploadId(), file.getObjectName(), file.getSize()
		));
	}
}
