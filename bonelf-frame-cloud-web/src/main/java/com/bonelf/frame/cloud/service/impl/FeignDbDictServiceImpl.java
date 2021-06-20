package com.bonelf.frame.cloud.service.impl;

import com.bonelf.frame.core.constant.CommonCacheConstant;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.frame.web.domain.bo.DictTextBO;
import com.bonelf.frame.web.domain.bo.DictValueBO;
import com.bonelf.frame.web.service.DbDictService;
import com.bonelf.support.feign.SupportFeignClient;
import com.bonelf.support.feign.domain.request.DictValueRequest;
import com.bonelf.support.feign.domain.response.DictTextResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * feign字典
 * </p>
 * @author bonelf
 * @since 2020/10/14 13:13
 */
// @ConditionalOnProperty(prefix = BonelfConstant.PROJECT_NAME, value = "mode", havingValue = "cloud")
@CacheConfig(cacheNames = CommonCacheConstant.CACHE_NAME_7_DAY)
@Service("feignDbDictServiceImpl")
@Slf4j
public class FeignDbDictServiceImpl implements DbDictService {
	@Autowired
	private SupportFeignClient supportFeignClient;
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;
	@Autowired
	private KeyGenerator keyGenerator;

	/**
	 * 实现缓存
	 * @param dictId
	 * @param itemValue
	 * @return
	 */
	@Override
	@Cacheable(value = CommonCacheConstant.DB_DICT, condition = "#result != null")
	public String queryDictTextByKey(String dictId, String itemValue) {
		Result<String> res = supportFeignClient.selectDictTextByItemValue(dictId, itemValue);
		if (res.getSuccess()) {
			return res.getResult();
		} else {
			return null;
		}
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
		Set<DictTextBO> texts = new HashSet<>();
		if (!query.isEmpty()) {
			Result<Set<DictTextResponse>> res = supportFeignClient.selectDictTextByItemValueBatch(query.stream().map(item -> {
				DictValueRequest req = new DictValueRequest();
				req.setDictId(item.getDictId());
				req.setItemValue(String.valueOf(item.getItemValue()));
				return req;
			}).collect(Collectors.toSet()));
			if (res.getSuccess()) {
				texts = res.getResult().stream().map(item -> {
					DictTextBO req = new DictTextBO();
					req.setDictId(item.getDictId());
					req.setItemText(item.getItemText());
					req.setItemValue(item.getItemValue());
					return req;
				}).collect(Collectors.toSet());
			}
		}
		result.putAll(texts.stream().collect(Collectors.toMap(
				item -> new DictValueBO(item.getDictId(), item.getItemValue()), DictTextBO::getItemText)));
		for (DictTextBO text : texts) {
			redisTemplate.opsForValue().set(this.getCacheName(text.getDictId(), text.getItemValue()),
					text.getItemText(), CommonCacheConstant.CACHE_NAME_7_DAY_TIME);
		}
		return result;
	}

	@Override
	public Map<DictValueBO, String> queryDictTextByKeyNoCache(Set<DictValueBO> dictText) {
		Result<Set<DictTextResponse>> res = supportFeignClient.selectDictTextByItemValueBatch(dictText.stream().map(item -> {
			DictValueRequest req = new DictValueRequest();
			req.setDictId(item.getDictId());
			req.setItemValue(String.valueOf(item.getItemValue()));
			return req;
		}).collect(Collectors.toSet()));
		if (res.getSuccess()) {
			return res.getResult().stream().collect(Collectors.toMap(
					item -> new DictValueBO(item.getDictId(), item.getItemValue()), DictTextResponse::getItemText));
		} else {
			return new HashMap<>();
		}
	}

	/**
	 * 缓存名称
	 * @param dictId
	 * @param itemValue
	 * @return
	 */
	private Object getCacheName(String dictId, String itemValue) {
		try {
			return CommonCacheConstant.DB_DICT + keyGenerator.generate(this,
					this.getClass().getMethod("queryDictTextByKey", String.class, String.class), dictId, itemValue);
		} catch (NoSuchMethodException e) {
			log.error("no method found", e);
			throw new BonelfException(CommonBizExceptionEnum.SERVER_ERROR);
		}
	}
}
