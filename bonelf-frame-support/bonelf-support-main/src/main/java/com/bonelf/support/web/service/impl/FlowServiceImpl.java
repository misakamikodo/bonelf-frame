package com.bonelf.support.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.bonelf.frame.core.auth.domain.Role;
import com.bonelf.frame.core.auth.domain.User;
import com.bonelf.frame.core.auth.service.AuthUserService;
import com.bonelf.frame.core.constant.UsernameType;
import com.bonelf.support.feign.domain.response.FlowInstanceResponse;
import com.bonelf.support.feign.domain.response.TaskResponse;
import com.bonelf.support.web.service.FlowService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流服务
 * @author bonelf
 * @date 2021/9/9 14:19
 */
@Slf4j
@Service
public class FlowServiceImpl implements FlowService {
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AuthUserService authUserService;
	@Autowired
	private ProcessEngine processEngine;

	@Override
	public void writeFlowDiagram(OutputStream outputStream, String processId, String imageFormat) throws IOException {
		ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
		// 流程走完的不显示图
		if (pi == null) {
			return;
		}
		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
		// 使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
		String instanceId = task.getProcessInstanceId();
		List<Execution> executions = runtimeService
				.createExecutionQuery()
				.processInstanceId(instanceId)
				.list();
		// 得到正在执行的Activity的Id
		List<String> activityIds = new ArrayList<>();
		List<String> flows = new ArrayList<>();
		for (Execution exe : executions) {
			List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
			activityIds.addAll(ids);
		}
		// 获取流程图
		BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
		ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
		ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
		try (InputStream in = diagramGenerator.generateDiagram(bpmnModel, imageFormat, activityIds, flows,
				engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(),
				engconf.getClassLoader(), 1.0, false)) {
			byte[] buf = new byte[1024];
			int length;
			while ((length = in.read(buf)) != -1) {
				outputStream.write(buf, 0, length);
			}
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	@Override
	public FlowInstanceResponse start(String userId, String diagramKey, Map<String, Object> payload) {
		Map<String, Object> map = new HashMap<>(payload);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(diagramKey, map);
		FlowInstanceResponse response = new FlowInstanceResponse();
		response.setProcessStatus(getProcessStatus(processInstance.getProcessInstanceId()));
		response.setProcessId(processInstance.getProcessInstanceId());
		response.setTask(tasks(null, processInstance.getProcessInstanceId()));
		return response;
	}

	@Override
	public FlowInstanceResponse flow(String taskId, Map<String, Object> payload) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			throw new RuntimeException("流程不存在");
		}
		//通过审核
		HashMap<String, Object> map = new HashMap<>(payload);
		taskService.complete(taskId, map);
		FlowInstanceResponse resp = new FlowInstanceResponse();
		// userId: task.getAssignee() 这里不传, 因为确定有权限
		resp.setProcessStatus(getProcessStatus(task.getProcessInstanceId()));
		resp.setProcessId(task.getProcessInstanceId());
		resp.setTask(tasks(null, task.getProcessInstanceId()));
		return resp;
	}

	private String getProcessStatus(@NonNull String processId) {
		List<ActivityInstance> activeActivities = this.getActiveActivity(processId);
		if (activeActivities.size() > 0) {
			// 图里的状态名：activeActivities.get(0).getActivityName()
			return activeActivities.get(0).getActivityId();
		} else {// 取结束状态
			HistoricActivityInstance endActivity = this.getEndHistoricActivity(processId);
			if (endActivity != null) {
				return endActivity.getActivityId();
			}
			log.warn("Cannot get the processInstance status for: {}", processId);
			return null;
		}
	}

	private List<ActivityInstance> getActiveActivity(@NonNull String processInstanceId) {
		return runtimeService.createActivityInstanceQuery().processInstanceId(processInstanceId).unfinished().list();
	}

	private HistoricActivityInstance getEndHistoricActivity(@NonNull String processInstanceId) {
		return historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
				.activityType(BpmnXMLConstants.ELEMENT_EVENT_END).finished().singleResult();
	}

	@Override
	public int remove(String processId, String reason) {
		// historyService.deleteHistoricProcessInstance(processId);
		if (StrUtil.isNotEmpty(processId)) {
			if (historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).finished()
					.count() > 0) {
				// 流程结束
				historyService.deleteHistoricProcessInstance(processId);
			} else {
				// 流程实例ID是否在运行期存在
				if (runtimeService.createProcessInstanceQuery().processInstanceId(processId).count() > 0) {
					runtimeService.deleteProcessInstance(processId, reason);
					historyService.deleteHistoricProcessInstance(processId);
				} else {
					log.warn("Remove process instance failure, because cannot find the process instance by id:{}",
							processId);
					return 0;
				}
			}
		}
		return 1;
	}

	@Override
	public List<TaskResponse> tasks(String userId, String processId) {
		// 用户已经签收、用户或用户所在组等待签收的任务
		TaskQuery taskQuery = taskService.createTaskQuery().active().orderByTaskCreateTime().desc();
		// if (StrUtil.isNotEmpty(diagramKey)) {
		// 	taskQuery.processDefinitionKey(diagramKey);
		// }
		if (StrUtil.isNotEmpty(processId)) {
			taskQuery.processInstanceId(processId);
		}
		if (StrUtil.isNotBlank(userId)) {
			User user = authUserService.getByUniqueId(userId, UsernameType.id);
			if (user.getRoles().size() > 0) {
				// 按权限过滤
				taskQuery.or().taskCandidateOrAssigned(userId).taskCandidateGroupIn(user.getRoles()
						.stream().map(Role::getCode).collect(Collectors.toList())).endOr();
			} else if (StrUtil.isNotBlank(userId)) {
				taskQuery.taskCandidateOrAssigned(userId);
			}
		}
		taskQuery.includeTaskLocalVariables().includeProcessVariables();
		return taskQuery.list().stream().map(item -> {
			TaskResponse data = new TaskResponse();
			data.setTaskId(item.getId());
			return data;
		}).collect(Collectors.toList());
	}

	@Override
	public List<String> processes(String diagramKey) {
		List<HistoricProcessInstance> processDefinitions = historyService.createHistoricProcessInstanceQuery()
				.processDefinitionKey(diagramKey)
				.list();
		return processDefinitions.stream().map(HistoricProcessInstance::getId)
				.collect(Collectors.toList());
	}
}
