package com.bonelf.frame.web.core.dict.service;

import com.bonelf.frame.web.core.dict.domain.DbDictValue;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 数据库自动工具
 * </p>
 * @author bonelf
 * @since 2020/10/14 13:13
 */
public interface DbDictService {

	/**
	 * 查询字典 缓存
	 * @param dictId 字典类型
	 * @param itemValue 字典值
	 * @return
	 */
	String queryDictTextByKey(String dictId, String itemValue);


	/**
	 * 查询字典 无缓存
	 * @param dictId
	 * @param itemValue
	 * @return
	 */
	String queryDictTextByKeyNoCache(String dictId, String itemValue);

	/**
	 * 查询字典 缓存
	 * @param dictText 字典
	 * @return key: dictId:value value: itemText
	 */
	Map<DbDictValue, String> queryDictTextByKey(Set<DbDictValue> dictText);


	/**
	 * 查询字典 无缓存
	 * @param dictText 字典
	 * @return key: dictId:value value: itemText
	 */
	Map<DbDictValue, String> queryDictTextByKeyNoCache(Set<DbDictValue> dictText);
}
