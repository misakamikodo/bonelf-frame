package com.bonelf.support.web.controller;


import com.bonelf.support.feign.domain.request.FlowRequest;
import com.bonelf.support.feign.domain.response.FlowInstanceResponse;
import com.bonelf.support.feign.domain.response.TaskResponse;
import com.bonelf.support.web.service.FlowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 工作流接口
 * 需要给对应的工作实体绑定 当前流程执行人（列表）、流程状态 方便查询（因为微服务不直接引入support）和流程ID
 * @author bonelf
 * @date 2021年9月9日
 **/
@Slf4j
@RestController
@Api(value = "工作流接口", tags = {"工作流"})
@RequestMapping("/flow")
public class FlowableController {
	@Autowired
	private FlowService flowService;

	@GetMapping("/show")
	@ApiOperation(value = "工作流图片")
	public void show(HttpServletResponse response, @RequestParam String processId) throws Exception {
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
		flowService.writeFlowDiagram(response.getOutputStream(), processId, "jpeg");
	}

	@PostMapping("/start")
	@ApiOperation(value = "开启工作流")
	public FlowInstanceResponse start(@RequestBody FlowRequest req) throws Exception {
		return flowService.start(req.getUserId(), req.getDiagramKey(), req.getPayload());
	}

	@GetMapping("/task")
	@ApiOperation(value = "当前task")
	public List<TaskResponse> task(@RequestParam String userId, @RequestParam String processId) throws Exception {
		return flowService.tasks(userId, processId);
	}

	@PostMapping("/flow")
	@ApiOperation(value = "驱动工作流")
	public FlowInstanceResponse flow(@RequestBody FlowRequest req) {
		return flowService.flow(req.getTaskId(), req.getPayload());
	}

	@PostMapping("/remove")
	@ApiOperation(value = "删除工作流")
	public int remove(@RequestParam String processId, @RequestParam(required = false, defaultValue = "N/A") String reason) {
		return flowService.remove(processId, reason);
	}

	@GetMapping("/test/allProcess")
	@ApiOperation(value = "测试用获取所有流程")
	public List<String> processes(@RequestParam String diagramKey) throws Exception {
		return flowService.processes(diagramKey);
	}
}
