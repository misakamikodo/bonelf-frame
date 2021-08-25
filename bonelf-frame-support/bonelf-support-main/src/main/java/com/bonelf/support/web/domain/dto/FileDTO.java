package com.bonelf.support.web.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.PrimitiveIterator;

/**
 * 文件上传DTO
 * @author bonelf
 */
@Data
public class FileDTO implements Serializable {
	/**
	 * 文件的来源ID，暂时没用
	 */
	private String uuid;
	/**
	 * 文件MD5
	 */
	private String md5;
	/**
	 * 文件在minio服务器上的ID
	 */
	private String uploadId;
	/**
	 * 是否已上传 可以用uploadId是否为空代替，增加字段友好交互
	 */
	private Boolean uploaded;
	/**
	 * 分片总数
	 */
	private Integer totalChunkCounts;
	/**
	 * 当前分片编号
	 */
	private Integer chunkNumber;
	/**
	 * 上传成功的分片编号 逗号分割
	 */
	private String chunks;
	/**
	 * 分片大小
	 */
	private Long size;
	/**
	 * 文件名称
	 */
	private String fileName;
	/**
	 * 对象名称
	 */
	private String objectName;
	/**
	 *
	 */
	private String etag;
	/**
	 * 已经完成上传的
	 */
	private String completedParts;
}
