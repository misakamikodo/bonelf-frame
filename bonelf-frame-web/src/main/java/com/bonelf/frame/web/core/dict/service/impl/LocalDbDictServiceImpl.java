package com.bonelf.frame.web.core.dict.service.impl;

import com.bonelf.frame.base.property.enums.ProjectMode;
import com.bonelf.frame.core.constant.CommonCacheConstant;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.frame.web.core.dict.domain.DbDictText;
import com.bonelf.frame.web.core.dict.domain.DbDictValue;
import com.bonelf.frame.web.core.dict.service.DbDictService;
import com.bonelf.frame.web.mapper.SysDictItemMapper;
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
	public Map<DbDictValue, String> queryDictTextByKey(Set<DbDictValue> dictText) {
		Set<DbDictValue> query = new HashSet<>();
		Map<DbDictValue, String> result = new HashMap<>();
		for (DbDictValue dv : dictText) {
			String cacheValue = (String)redisTemplate.opsForValue().get(
					this.getCacheName(dv.getDictId(), String.valueOf(dv.getItemValue())));
			if (cacheValue == null) {
				query.add(dv);
			} else {
				result.put(dv, cacheValue);
			}
		}
		Set<DbDictText> texts = sysDictItemMapper.selectDictTextByItemValueBatch(query);
		result.putAll(texts.stream().collect(Collectors.toMap(item -> new DbDictValue(item.getDictId(), item.getItemValue()), DbDictText::getItemText)));
		for (DbDictText text : texts) {
			redisTemplate.opsForValue().set(this.getCacheName(text.getDictId(), text.getItemValue()),
					text.getItemText(), CommonCacheConstant.CACHE_NAME_7_DAY_TIME.toDays());
		}
		return result;
	}

	@Override
	public Map<DbDictValue, String> queryDictTextByKeyNoCache(Set<DbDictValue> dictText) {
		Set<DbDictText> texts = sysDictItemMapper.selectDictTextByItemValueBatch(dictText);
		return texts.stream().collect(Collectors.toMap(item -> new DbDictValue(item.getDictId(), item.getItemValue()), DbDictText::getItemText));
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
					keyGenerator.generate(this,
							this.getClass().getMethod("queryDictTextByKey", String.class, String.class),
							dictId, itemValue);
		} catch (NoSuchMethodException e) {
			log.error("no method found", e);
			throw new BonelfException(CommonBizExceptionEnum.SERVER_ERROR);
		}
	}
}
