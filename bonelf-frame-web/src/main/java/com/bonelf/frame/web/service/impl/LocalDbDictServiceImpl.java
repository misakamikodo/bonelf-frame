package com.bonelf.frame.web.service.impl;

import com.bonelf.frame.base.property.enums.ProjectMode;
import com.bonelf.frame.core.constant.CommonCacheConstant;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.frame.web.domain.bo.DictTextBO;
import com.bonelf.frame.web.domain.bo.DictValueBO;
import com.bonelf.frame.web.mapper.SysDictItemMapper;
import com.bonelf.frame.web.service.DbDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 数据库字典自动填充工具
 * </p>
 * @author bonelf
 * @see ProjectMode
 * @since 2020/10/14 13:13
 */
// @ConditionalOnProperty(prefix = BonelfConstant.PROJECT_NAME, value = "mode", havingValue = "singlePoint", matchIfMissing = true)
@CacheConfig(cacheNames = CommonCacheConstant.CACHE_NAME_7_DAY)
// @Service("localDbDictServiceImpl")
@Slf4j
public class LocalDbDictServiceImpl implements DbDictService {
	private final SysDictItemMapper sysDictItemMapper;
	private final RedisTemplate<Object, Object> redisTemplate;
	private final KeyGenerator keyGenerator;

	public LocalDbDictServiceImpl(SysDictItemMapper sysDictItemMapper, RedisTemplate<Object, Object> redisTemplate, KeyGenerator keyGenerator) {
		this.sysDictItemMapper = sysDictItemMapper;
		this.redisTemplate = redisTemplate;
		this.keyGenerator = keyGenerator;
	}

	/**
	 * 实现缓存
	 * @param dictId
	 * @param itemValue
	 * @return
	 */
	@Override
	@Cacheable(value = CommonCacheConstant.DB_DICT, condition = "#result != null")
	public String queryDictTextByKey(String dictId, String itemValue) {
		return sysDictItemMapper.selectDictTextByItemValue(dictId, itemValue);
	}

	@Override
	public String queryDictTextByKeyNoCache(String dictId, String itemValue) {
		return queryDictTextByKey(dictId, itemValue);
	}

	@Override
	public Map<DictValueBO, String> queryDictTextByKey(Set<DictValueBO> dictText) {
		Set<DictValueBO> query = new HashSet<>();
		Map<DictValueBO, String> result = new HashMap<>();
		for (DictValueBO dv : dictText) {
			String cacheValue = (String)redisTemplate.opsForValue().get(
					this.getCacheName(dv.getDictId(), String.valueOf(dv.getItemValue())));
			if (cacheValue == null) {
				query.add(dv);
			} else {
				result.put(dv, cacheValue);
			}
		}
		Set<DictTextBO> texts = sysDictItemMapper.selectDictTextByItemValueBatch(query);
		result.putAll(texts.stream().collect(Collectors.toMap(item -> new DictValueBO(item.getDictId(), item.getItemValue()), DictTextBO::getItemText)));
		for (DictTextBO text : texts) {
			redisTemplate.opsForValue().set(this.getCacheName(text.getDictId(), text.getItemValue()),
					text.getItemText(), CommonCacheConstant.CACHE_NAME_7_DAY_TIME.toDays());
		}
		return result;
	}

	/**
	 * 缓存名称
	 * @param dictId
	 * @param itemValue
	 * @return
	 */
	private Object getCacheName(String dictId, String itemValue) {
		try {
			return CommonCacheConstant.DB_DICT +
					keyGenerator.generate(this, this.getClass().getMethod("queryDictTextByKey", String.class, String.class), dictId, itemValue);
		} catch (NoSuchMethodException e) {
			log.error("no method found", e);
			throw new BonelfException(CommonBizExceptionEnum.SERVER_ERROR);
		}
	}

	@Override
	public Map<DictValueBO, String> queryDictTextByKeyNoCache(Set<DictValueBO> dictText) {
		Set<DictTextBO> texts = sysDictItemMapper.selectDictTextByItemValueBatch(dictText);
		return texts.stream().collect(Collectors.toMap(item -> new DictValueBO(item.getDictId(), item.getItemValue()), DictTextBO::getItemText));
	}
}
