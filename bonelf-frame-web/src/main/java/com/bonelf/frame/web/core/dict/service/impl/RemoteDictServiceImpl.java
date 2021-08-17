package com.bonelf.frame.web.core.dict.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.bonelf.cicada.util.Md5CryptUtil;
import com.bonelf.frame.base.util.JsonUtil;
import com.bonelf.frame.core.constant.CommonCacheConstant;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.web.core.dict.domain.RemoteDictValue;
import com.bonelf.frame.web.core.dict.service.RemoteDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 远程字典服务实现
 * @author bonelf
 * @date 2021/8/11 14:26
 */
@Slf4j
@Service
public class RemoteDictServiceImpl implements RemoteDictService {
	private static final String MD5_SALT = "=bonelfRT=";

	@Autowired(required = false)
	private RestTemplate restTemplate;
	@Autowired(required = false)
	private KeyGenerator keyGenerator;
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;

	/**
	 * @param clzMethodMd5 类名-方法 md5
	 * @return
	 */
	@Cacheable(value = CommonCacheConstant.REMOTE_DICT,
			key = CommonCacheConstant.REMOTE_DICT + ":#p0-#p1-#p2",
			condition = "#result != null")
	public String queryDictTextByKey(String addrMd5,
									 String clzMethodMd5,
									 String itemValue,
									 String addr,
									 String methodClz,
									 String methodName) {
		Collection<String> res = queryDictTextByKeySingleAddr(addr, methodClz, methodName,
				CollectionUtil.set(false, itemValue)).values();
		if (res.size() == 0) {
			return null;
		}
		return new ArrayList<>(res).get(0);
	}

	@Override
	public Map<RemoteDictValue, String> queryDictTextByKeyNoCache(Set<RemoteDictValue> noCacheQuery) {
		// 先按同addr的分组
		Map<RemoteDictValue, Set<RemoteDictValue>> dbGroup = noCacheQuery.stream().collect(
				Collectors.groupingBy(item -> {
					RemoteDictValue value = new RemoteDictValue();
					value.setAddr(item.getAddr());
					value.setMethodClz(item.getMethodClz());
					value.setMethodName(item.getMethodName());
					return value;
				}, Collectors.toSet()));
		Map<RemoteDictValue, String> result = new HashMap<>();
		for (Map.Entry<RemoteDictValue, Set<RemoteDictValue>> entry : dbGroup.entrySet()) {
			result.putAll(
					this.queryDictTextByKeySingleAddr(entry.getKey().getAddr(),
							entry.getKey().getMethodClz(),
							entry.getKey().getMethodName(),
							entry.getValue().stream().map(RemoteDictValue::getItemValue).collect(Collectors.toSet()))
			);
		}
		return result;
	}

	private Map<RemoteDictValue, String> queryDictTextByKeySingleAddr(String addr,
																	  String methodClz,
																	  String methodName,
																	  Set<Object> itemValues) {
		// List<Map<String, Object>> map = ;
		String url = addr.replace("{values}", itemValues.stream().map(String::valueOf).collect(Collectors.joining(",")));
		ResponseEntity<JSONObject> resp = restTemplate.getForEntity(url, JSONObject.class);
		String respString;
		if (resp.getStatusCode().is2xxSuccessful()) {
			respString = resp.getBody().toString();
		} else {
			throw new BonelfException(String.format("请求%s出错", url));
		}
		Map<String, Object> data;
		if (StrUtil.isNotBlank(methodClz) && StrUtil.isNotBlank(methodName)) {
			try {
				data = ReflectUtil.invokeStatic(
						ClassLoader.getSystemClassLoader().loadClass(methodClz).getMethod(methodName, String.class),
						respString);
			} catch (NoSuchMethodException | ClassNotFoundException e) {
				log.error("没有找到方法{}#{}", methodClz, methodName);
				data = new HashMap<>();
			}
		} else {
			// TODO String？？
			Result<?> respResult = JsonUtil.parse(respString, Result.class);
			if (respResult == null) {
				data = JsonUtil.toMap(respString);
			} else {
				data = (Map)respResult.getResult();
			}
		}
		return data == null ? new HashMap<>() :
				keyValueMap2DictMap(addr, methodClz, methodName, data);
	}

	private Map<RemoteDictValue, String> keyValueMap2DictMap(String addr,
															 String methodClz,
															 String methodName,
															 Map<String, Object> data) {
		return data.entrySet().stream().collect(Collectors.toMap(
				item -> {
					RemoteDictValue k = new RemoteDictValue();
					k.setAddr(addr);
					k.setMethodName(methodName);
					k.setMethodClz(methodClz);
					k.setItemValue(item.getKey());
					return k;
				}, item -> (String)item.getValue()
		));
	}

	@Override
	public Map<RemoteDictValue, String> queryDictTextByKey(Set<RemoteDictValue> cacheQuery) {
		Map<RemoteDictValue, String> result = new HashMap<>();
		// 先按同addr的分组
		Map<RemoteDictValue, Set<RemoteDictValue>> dbGroup = cacheQuery.stream().collect(
				Collectors.groupingBy(item -> {
					RemoteDictValue value = new RemoteDictValue();
					value.setAddr(item.getAddr());
					value.setMethodClz(item.getMethodClz());
					value.setMethodName(item.getMethodName());
					return value;
				}, Collectors.toSet()));
		// 缓存没有需要查询的
		for (Map.Entry<RemoteDictValue, Set<RemoteDictValue>> entry : dbGroup.entrySet()) {
			// 初始化
			Set<Object> curQuerySet = new HashSet<>();
			Set<RemoteDictValue> values = entry.getValue();
			for (RemoteDictValue tv : values) {
				String cacheValue = (String)redisTemplate.opsForValue().get(
						this.getCacheName(tv.getAddr(), String.valueOf(tv.getItemValue())));
				if (cacheValue == null) {
					curQuerySet.add(tv.getItemValue());
				} else {
					result.put(tv, cacheValue);
				}
			}
			if (!curQuerySet.isEmpty()) {
				result.putAll(this.queryDictTextByKeySingleAddr(
						entry.getKey().getAddr(),
						entry.getKey().getMethodClz(),
						entry.getKey().getMethodName(),
						curQuerySet));
			}
		}
		return result;
	}

	/**
	 * 缓存名称
	 * @param addr
	 * @param itemValue
	 * @return
	 */
	private Object getCacheName(String addr, String itemValue) {
		// try {
		// return CommonCacheConstant.TABLE_DICT +
		// 		keyGenerator.generate(this,
		// 				this.getClass().getMethod("queryDictTextByKey", String.class, String.class),
		// 				addr, itemValue);
		return CommonCacheConstant.REMOTE_DICT + ":" +
				Md5CryptUtil.encrypt(addr, MD5_SALT) + "-" + itemValue;
		// } catch (NoSuchMethodException e) {
		// 	log.error("no method found", e);
		// 	throw new BonelfException(CommonBizExceptionEnum.SERVER_ERROR);
		// }
	}
}
