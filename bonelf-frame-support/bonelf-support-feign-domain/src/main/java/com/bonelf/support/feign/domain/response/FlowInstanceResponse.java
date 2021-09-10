package com.bonelf.support.feign.domain.response;

import lombok.Data;

import java.util.List;

/**
 * 流程实体
 * @author bonelf
 * @date 2021/9/9 14:56
 */
@Data
public class FlowInstanceResponse {

	private String processId;

	private List<TaskResponse> task;

	private String processStatus;
}
