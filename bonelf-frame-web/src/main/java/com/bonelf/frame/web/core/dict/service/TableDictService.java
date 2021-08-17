package com.bonelf.frame.web.core.dict.service;

import com.bonelf.frame.web.core.dict.domain.TableDictValue;

import java.util.Map;
import java.util.Set;

/**
 * 表字典服务
 * @author bonelf
 * @date 2021/8/11 14:26
 */
public interface TableDictService {

	/**
	 * 批量查不过缓存
	 * @param noCacheQuery
	 * @return
	 */
	Map<TableDictValue, String> queryDictTextByKeyNoCache(Set<TableDictValue> noCacheQuery);

	/**
	 * 批量查过缓存
	 * @param cacheQuery
	 * @return
	 */
	Map<TableDictValue, String> queryDictTextByKey(Set<TableDictValue> cacheQuery);
}
