package com.bonelf.frame.web.core.dict.service;

import com.bonelf.frame.web.core.dict.domain.RemoteDictValue;

import java.util.Map;
import java.util.Set;

/**
 * 远程字典服务
 * @author bonelf
 * @date 2021/8/11 14:26
 */
public interface RemoteDictService {
	/**
	 * 不用缓存拆线呢远程字典信息
	 * @param noCacheQuery
	 * @return
	 */
	Map<RemoteDictValue, String> queryDictTextByKeyNoCache(Set<RemoteDictValue> noCacheQuery);

	/**
	 * 不用缓存拆线呢远程字典信息
	 * @param cacheQuery
	 * @return
	 */
	Map<RemoteDictValue, String> queryDictTextByKey(Set<RemoteDictValue> cacheQuery);
}
