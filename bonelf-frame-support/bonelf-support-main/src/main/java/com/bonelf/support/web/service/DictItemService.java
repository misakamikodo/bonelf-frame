package com.bonelf.support.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bonelf.frame.web.domain.entity.SysDictItem;
import com.bonelf.support.feign.domain.request.DictValueRequest;
import com.bonelf.support.feign.domain.response.DictTextResponse;

import java.util.Set;

/**
 * 字典明细服务接口
 * @author ccy
 * @date 2021/6/16 9:30
 */
public interface DictItemService extends IService<SysDictItem>{

	/**
	 * 单字典获取
	 * @param dictId
	 * @param itemValue
	 * @return
	 */
	String getTextByValue(String dictId, String itemValue);

	/**
	 * 多记录
	 * @param query
	 * @return
	 */
	Set<DictTextResponse> getTextByValueBatch(Set<DictValueRequest> query);
}
