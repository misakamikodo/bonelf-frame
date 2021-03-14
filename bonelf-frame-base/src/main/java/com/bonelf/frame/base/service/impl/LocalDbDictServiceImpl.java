package com.bonelf.frame.base.service.impl;

import com.bonelf.frame.base.service.DbDictService;
import com.bonelf.frame.base.property.enums.ProjectMode;
import com.bonelf.frame.core.constant.BonelfConstant;
import com.bonelf.frame.core.constant.CommonCacheConstant;
import com.bonelf.frame.core.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * TODO 数据库自动工具
 * </p>
 * @see ProjectMode
 * @author bonelf
 * @since 2020/10/14 13:13
 */
@ConditionalOnProperty(prefix = BonelfConstant.PROJECT_NAME, value = "mode", havingValue = "singlePoint", matchIfMissing = true)
@CacheConfig(cacheNames = CommonCacheConstant.CACHE_NAME_7_DAY)
@Service
public class LocalDbDictServiceImpl implements DbDictService {
	/**
	 *  使用restTemplate对system模块发起请求 获取dict数据
	 */
	@Autowired
	private RestTemplate restTemplate;

	// @Autowired
	// private SupportFeignClient supportFeignClient;

	/**
	 * 实现缓存
	 * @param code
	 * @param value
	 * @return
	 */
	@Cacheable(value = CommonCacheConstant.DB_DICT, condition = "!'-'.equals(#result)")
	public String queryDictTextByKey(String code, String value) {
		// Result<String> result = supportFeignClient.queryDictTextByKey(code, value);
		Result<String> result = Result.ok("-");
		return result != null && result.getSuccess() ? result.getResult() : "-";
	}

	public String queryDictTextByKeyNoCache(String code, String value) {
		// Result<String> result = supportFeignClient.queryDictTextByKey(code, value);
		Result<String> result = Result.ok("-");
		return result != null && result.getSuccess() ? result.getResult() : "-";
	}
}
