package com.bonelf.support.web.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO 系统服务每日统计点击量
 * @author
 */
@Slf4j
@Component
public class UpdateSystemSpuClickJob implements Job {
	// @Autowired
	// private MqProducerService mqService;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		// mqService.send(ChannelEnum.SYSTEM, MQSendTag.SPU_CLICK_SUM, "");
	}
}
