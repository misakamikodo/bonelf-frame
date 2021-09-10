package com.bonelf.support.web.service;

import com.bonelf.support.feign.domain.response.FlowInstanceResponse;
import com.bonelf.support.feign.domain.response.TaskResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 工作流服务
 * @author bonelf
 * @date 2021/9/9 14:19
 */
public interface FlowService {
	/**
	 * 输出流
	 * @param outputStream
	 * @param processId
	 * @param imageFormat
	 */
	void writeFlowDiagram(OutputStream outputStream, String processId, String imageFormat) throws IOException;

	/**
	 * 开启流程
	 * @param userId
	 * @param diagramKey
	 * @param payload
	 */
	FlowInstanceResponse start(String userId, String diagramKey, Map<String, Object> payload);

	/**
	 * 驱动工作流
	 * @param taskId 流程ID
	 * @param payload
	 */
	FlowInstanceResponse flow(String taskId, Map<String, Object> payload);

	/**
	 * 删除工作流
	 * @param processId
	 * @param reason
	 */
	int remove(String processId, String reason);

	/**
	 * 任务列表
	 * @param userId
	 * @param processId
	 * @return
	 */
	List<TaskResponse> tasks(String userId, String processId);

	/**
	 * 所有流程
	 * @return
	 * @param diagramKey
	 */
	List<String> processes(String diagramKey);
}
