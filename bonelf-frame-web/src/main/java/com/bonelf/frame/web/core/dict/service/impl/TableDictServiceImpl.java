package com.bonelf.frame.web.core.dict.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.bonelf.cicada.util.Md5CryptUtil;
import com.bonelf.frame.core.constant.CommonCacheConstant;
import com.bonelf.frame.web.core.dict.domain.TableDictValue;
import com.bonelf.frame.web.core.dict.service.TableDictService;
import com.bonelf.frame.web.mapper.SqlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表字典服务实现
 * @author bonelf
 * @date 2021/8/11 14:26
 */
@Slf4j
@Service
public class TableDictServiceImpl implements TableDictService {
	private static final String MD5_SALT = "=bonelfTB=";

	@Autowired(required = false)
	private SqlMapper sqlMapper;
	@Autowired(required = false)
	private KeyGenerator keyGenerator;
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;

	@Cacheable(value = CommonCacheConstant.TABLE_DICT, key = CommonCacheConstant.TABLE_DICT + ":#p1-#p2", condition = "#result != null")
	public String queryDictTextByKey(String sql, String sqlMd5, String itemValue) {
		Collection<String> res = queryDictTextByKeySingleSql(sql,
				CollectionUtil.set(false, itemValue)).values();
		if (res.size() == 0) {
			return null;
		}
		return new ArrayList<>(res).get(0);
	}

	@Override
	public Map<TableDictValue, String> queryDictTextByKeyNoCache(Set<TableDictValue> noCacheQuery) {
		// 先按同sql的分组
		Map<String, Set<TableDictValue>> dbGroup = noCacheQuery.stream().collect(
				Collectors.groupingBy(TableDictValue::getSql, Collectors.toSet()));
		Map<TableDictValue, String> result = new HashMap<>();
		for (Map.Entry<String, Set<TableDictValue>> entry : dbGroup.entrySet()) {
			result.putAll(this.queryDictTextByKeySingleSql(entry.getKey(),
					entry.getValue().stream().map(TableDictValue::getItemValue).collect(Collectors.toSet())));
		}
		return result;
	}

	private Map<TableDictValue, String> queryDictTextByKeySingleSql(String sql, Set<Object> itemValues) {
		List<Map<String, Object>> map = sqlMapper.dynamicsQuery(sql.replace(":values",
				itemValues.stream().map(String::valueOf).collect(Collectors.joining(","))));
		return map.stream().collect(Collectors.toMap(
				item -> {
					TableDictValue k = new TableDictValue();
					k.setSql(sql);
					k.setItemValue(item.get("key"));
					return k;
				}, item -> (String)item.get("value")
		));
	}

	@Override
	public Map<TableDictValue, String> queryDictTextByKey(Set<TableDictValue> cacheQuery) {
		if (sqlMapper == null) {
			throw new UnsupportedOperationException("you can't use @TableDict this project, because mybatis is not enable");
		}
		Map<TableDictValue, String> result = new HashMap<>();
		// 先按同sql的分组
		Map<String, Set<TableDictValue>> dbGroup = cacheQuery.stream().collect(
				Collectors.groupingBy(TableDictValue::getSql, Collectors.toSet()));
		// 缓存没有需要查询的
		for (Map.Entry<String, Set<TableDictValue>> entry : dbGroup.entrySet()) {
			// 初始化
			Set<Object> curQuerySet = new HashSet<>();
			Set<TableDictValue> values = entry.getValue();
			for (TableDictValue tv : values) {
				String cacheValue = (String)redisTemplate.opsForValue().get(
						this.getCacheName(tv.getSql(), String.valueOf(tv.getItemValue())));
				if (cacheValue == null) {
					curQuerySet.add(tv.getItemValue());
				} else {
					result.put(tv, cacheValue);
				}
			}
			if (!curQuerySet.isEmpty()) {
				result.putAll(this.queryDictTextByKeySingleSql(entry.getKey(), curQuerySet));
			}
		}
		return result;
	}

	/**
	 * 缓存名称
	 * @param sql
	 * @param itemValue
	 * @return
	 */
	private Object getCacheName(String sql, String itemValue) {
		// try {
		// return CommonCacheConstant.TABLE_DICT +
		// 		keyGenerator.generate(this,
		// 				this.getClass().getMethod("queryDictTextByKey", String.class, String.class),
		// 				sql, itemValue);
		return CommonCacheConstant.TABLE_DICT + ":" +
				Md5CryptUtil.encrypt(sql, MD5_SALT) + "-" + itemValue;
		// } catch (NoSuchMethodException e) {
		// 	log.error("no method found", e);
		// 	throw new BonelfException(CommonBizExceptionEnum.SERVER_ERROR);
		// }
	}
}
