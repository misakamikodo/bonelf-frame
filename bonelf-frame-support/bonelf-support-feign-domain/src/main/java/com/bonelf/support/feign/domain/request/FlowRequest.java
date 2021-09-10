package com.bonelf.support.feign.domain.request;

import lombok.Data;

import java.util.Map;

/**
 * 流程实体
 * @author bonelf
 * @date 2021/9/9 14:56
 */
@Data
public class FlowRequest {
	/**
	 * 流程ID
	 */
	private String processId;
	/**
	 * 任务
	 */
	private String taskId;
	/**
	 * 业务编号
	 */
	private String userId;
	/**
	 * 流程图
	 */
	private String diagramKey;
	/**
	 * 驱动参数
	 */
	private Map<String, Object> payload;
}
