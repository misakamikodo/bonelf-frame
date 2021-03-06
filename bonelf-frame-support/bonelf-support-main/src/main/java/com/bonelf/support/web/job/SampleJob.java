package com.bonelf.support.web.job;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * 示例不带参定时任务
 * @author
 */
@Slf4j
@Component
public class SampleJob implements Job {

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		log.info("普通定时任务 SampleJob !  时间:" + DateUtil.now());
	}
}
