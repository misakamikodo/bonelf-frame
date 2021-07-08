/*
 * Copyright (c) 2021. Bonelf.
 */

package com.bonelf.frame.web.core.advice;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bonelf.cicada.util.EnumUtil;
import com.bonelf.frame.base.util.JsonUtil;
import com.bonelf.frame.base.util.SpringContextUtils;
import com.bonelf.frame.core.constant.CommonCacheConstant;
import com.bonelf.frame.core.dict.DbDict;
import com.bonelf.frame.core.dict.DictField;
import com.bonelf.frame.core.dict.EnumDict;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.web.aop.annotation.TableDict;
import com.bonelf.frame.web.constant.ResultCostAttr;
import com.bonelf.frame.web.domain.SimplePageInfo;
import com.bonelf.frame.web.domain.bo.DictValueBO;
import com.bonelf.frame.web.mapper.SqlMapper;
import com.bonelf.frame.web.mapper.SysDictItemMapper;
import com.bonelf.frame.web.service.DbDictService;
import com.bonelf.frame.web.service.impl.LocalDbDictServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 控制器返回数据
 * annotation和basePackages同时定义取了并集，所以不使用 annotations = {RestController.class, ResponseBody.class},
 * 为了跳过/v2/api-docs类似三方接口（XXX 可以尝试仅处理/api 开头的url）
 * @author bonelf
 * </p>
 * @since 2021/1/13 14:55
 */
@Slf4j
@RestControllerAdvice(
		basePackages = {"com.bonelf"}
)
public class RestControllerResultAdvice implements ResponseBodyAdvice<Object> {
	/**
	 * queryDictTextByKey的@Cacheable的Aop生效，否则也可以注入自己把方法写类里面，但是我不这么做
	 */
	@Autowired
	private SysDictItemMapper sysDictItemMapper;
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;
	@Autowired
	private KeyGenerator keyGenerator;
	/**
	 * new LocalDbDictServiceImpl(sysDictItemMapper, redisTemplate, keyGenerator); 这种成员都是null
	 */
	@Autowired(required = false)
	private DbDictService dbDictService;
	@Autowired
	private SqlMapper sqlMapper;

	@PostConstruct
	public void initDbDictService() {
		if (dbDictService == null) {
			dbDictService = new LocalDbDictServiceImpl(sysDictItemMapper, redisTemplate, keyGenerator);
		}
		// Map<String, DbDictService> dbDictServiceMap =
		// 		applicationContext.getBeansOfType(DbDictService.class);
		// if (dbDictServiceMap.size() > 1 || !dbDictServiceMap.containsKey("localDbDictServiceImpl")) {
		// 	// 使用feign定义
		// 	for (Map.Entry<String, DbDictService> entry : dbDictServiceMap.entrySet()) {
		// 		if (!"localDbDictServiceImpl".equals(entry.getKey())) {
		// 			this.dbDictService = entry.getValue();
		// 			break;
		// 		}
		// 	}
		// } else {
		// 	this.dbDictService = dbDictServiceMap.get("localDbDictServiceImpl");
		// }
	}

	@Override
	public boolean supports(@NonNull MethodParameter methodParameter,
							@NonNull Class<? extends HttpMessageConverter<?>> aClass) {
		// String类型(stringHttpMessageConverter)不支持
		// java.lang.ClassCastException: com.bonelf.frame.core.domain.Result cannot be cast to java.lang.String(因为还是走的stringHttpMessageConverter)
		// 这里只对可序列化Json做处理
		return MappingJackson2HttpMessageConverter.class.isAssignableFrom(aClass);
	}

	@Override
	public Object beforeBodyWrite(Object body,
								  @NonNull MethodParameter methodParameter,
								  @NonNull MediaType mediaType,
								  @NonNull Class<? extends HttpMessageConverter<?>> aClass,
								  @NonNull ServerHttpRequest request,
								  @NonNull ServerHttpResponse response) {
		Result<?> result;
		if (body instanceof Result) {
			result = (Result<?>)body;
		} else {
			result = Result.ok(body);
			ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
				HttpServletRequest servletRequest = attributes.getRequest();
				result.setCost((Long)servletRequest.getAttribute(ResultCostAttr.COST));
			}
		}
		// JsonNode result = objectMapper.valueToTree(target);
		if (SpringContextUtils.isProdProfile()) {
			result.setDevMessage(null);
		}
		// 翻译字典
		long time1 = System.currentTimeMillis();
		long time2 = System.currentTimeMillis();
		//log.debug("supportFeignClient获取数据 耗时：" + (time2 - time1) + "ms");
		long start = System.currentTimeMillis();
		this.parseDictText(result);
		long end = System.currentTimeMillis();
		return result;
	}


	/**
	 * 本方法针对返回对象为Result 的IPage的分页列表数据进行动态字典注入
	 * 例输入当前返回值的就会多出一个sex_{nameSuffix}字段
	 * @param record
	 */
	private <T> void parseDictText(@NonNull T record) {
		//对POJO解析
		Field[] fields = ReflectUtil.getFields(record.getClass());
		if (ArrayUtil.isEmpty(fields)) {
			return;
		}
		List<DbDictFieldHolder> dictIdFieldList = new ArrayList<>();
		for (Field field : fields) {
			Object fieldValue = ReflectUtil.getFieldValue(record, field.getName());
			if (fieldValue == null) {
				continue;
			}
			// 对每个field进行判断
			DbDict dbDict = field.getAnnotation(DbDict.class);
			if (dbDict != null) {
				String code = dbDict.value();
				String nameSuffix = dbDict.nameSuffix();
				boolean cached = dbDict.cached();
				// 翻译字典值对应的txt
				dictIdFieldList.add(new DbDictFieldHolder(code, field, nameSuffix, cached));
			}
			EnumDict enumDict = field.getAnnotation(EnumDict.class);
			if (enumDict != null) {
				decorateEnumDict(record, field, fieldValue, enumDict);
			}
			TableDict tableDict = field.getAnnotation(TableDict.class);
			if (tableDict != null) {
				decorateTableDict(record, field, fieldValue, tableDict);
			}
			DictField dictField = field.getAnnotation(DictField.class);
			if (dictField != null) {
				if (dictField.getClass() == record.getClass()) {
					log.error("DictField添加的属性Class类型和当前类相同将引起StackOverflowError，不予自动装配字典，请手动添加子属性的字典值");
					return;
				}
				String recordStr = JsonUtil.toJson(fieldValue);
				if (!JsonUtil.isJsonObj(recordStr)) {
					//不解析String、int等等非POJO对象
					return;
				}
				if (fieldValue instanceof Collection) {
					Collection<?> collection = (Collection<?>)fieldValue;
					for (Object c : collection) {
						parseDictText(c);
					}
					return;
				} else if (fieldValue.getClass().isArray()) {
					Object[] objects = (Object[])fieldValue;
					for (Object c : objects) {
						parseDictText(c);
					}
					return;
				} else if (fieldValue instanceof IPage) {
					IPage<?> page = (IPage<?>)fieldValue;
					for (Object c : page.getRecords()) {
						parseDictText(c);
					}
					return;
				} else if (fieldValue instanceof SimplePageInfo) {
					SimplePageInfo<?> page = (SimplePageInfo<?>)fieldValue;
					for (Object c : page.getRecords()) {
						parseDictText(c);
					}
					return;
				} else {
					parseDictText(fieldValue);
				}
			}
		}
		// 翻译数据库字典
		if (!dictIdFieldList.isEmpty()) {
			decorateDbDict(record, dictIdFieldList);
		}
	}

	/**
	 * 字典表字典
	 * @param record
	 * @param dictIdFieldList 字典批量操作list
	 * @param <T>
	 */
	private <T> void decorateDbDict(T record, List<DbDictFieldHolder> dictIdFieldList) {
		Set<DictValueBO> cacheQuery = new HashSet<>();
		Set<DictValueBO> noCacheQuery = new HashSet<>();
		for (DbDictFieldHolder entry : dictIdFieldList) {
			Object fieldValue = ReflectUtil.getFieldValue(record, entry.field.getName());
			DictValueBO item = new DictValueBO();
			item.setItemValue(fieldValue);
			item.setDictId(entry.dictId);
			if(entry.cached){
				cacheQuery.add(item);
			} else {
				noCacheQuery.add(item);
			}
		}
		if (!noCacheQuery.isEmpty()) {
			Map<DictValueBO, String> resNoCached = dbDictService.queryDictTextByKeyNoCache(noCacheQuery);
			wrapDbDictValue2Field(record, dictIdFieldList, resNoCached);
		}
		if (!cacheQuery.isEmpty()) {
			Map<DictValueBO, String> resCached = dbDictService.queryDictTextByKey(cacheQuery);
			wrapDbDictValue2Field(record, dictIdFieldList, resCached);
		}
	}

	/**
	 * 封装字典信息到对象
	 * @param record
	 * @param dictIdFieldList
	 * @param resCached
	 * @param <T>
	 */
	private <T> void wrapDbDictValue2Field(T record, List<DbDictFieldHolder> dictIdFieldList,
										   Map<DictValueBO, String> resCached) {
		Map<String, List<DbDictFieldHolder>> group = dictIdFieldList.stream().collect(
				Collectors.groupingBy(item -> item.dictId)
		);
		for (Map.Entry<DictValueBO, String> entry : resCached.entrySet()) {
			String textValue = entry.getValue();
			String dictId = entry.getKey().getDictId();
			if (!group.containsKey(dictId)) {
				continue;
			}
			for (DbDictFieldHolder holder : group.get(dictId)) {
				String name = holder.field.getName();
				String nameSuffix = holder.nameSuffix;
				try {
					ReflectUtil.setFieldValue(record,
							name + nameSuffix,
							textValue);
				} catch (UtilException | IllegalArgumentException e) {
					log.warn("对象需要转字典的对应Field找不到：{}", name + nameSuffix);
				}
			}
		}
	}

	/**
	 * 表字典
	 * @param record
	 * @param field
	 * @param fieldValue
	 * @param tableDict
	 * @param <T>
	 */
	private <T> void decorateTableDict(T record, Field field, Object fieldValue, TableDict tableDict) {
		String nameSuffix = tableDict.nameSuffix();
		TableName tableName = tableDict.value().getAnnotation(TableName.class);
		String textValue = "-";
		if (tableName != null) {
			//翻译字典值对应的txt
			textValue = translateTableDictVal(fieldValue, tableDict, tableName, tableDict.cached());
		}
		try {
			ReflectUtil.setFieldValue(record, field.getName() + nameSuffix, textValue);
		} catch (UtilException | IllegalArgumentException e) {
			log.warn("对象需要转字典的对应Field找不到：{}，请检查类型名称是否正确和类型是否为String", field.getName() + nameSuffix);
		}
	}

	/**
	 * 枚举字典
	 * @param record
	 * @param field
	 * @param fieldValue
	 * @param enumDict
	 * @param <T>
	 */
	private <T> void decorateEnumDict(@NonNull T record, Field field, Object fieldValue, EnumDict enumDict) {
		String nameSuffix = enumDict.nameSuffix();
		//翻译字典值对应的txt
		String textValue = EnumUtil.getEnumString(fieldValue, enumDict.value());
		try {
			ReflectUtil.setFieldValue(record, field.getName() + nameSuffix, textValue);
		} catch (UtilException | IllegalArgumentException e) {
			log.warn("对象需要转字典的对应Field找不到：{}，请检查类型名称是否正确和类型是否为String", field.getName() + nameSuffix);
		}
	}

	/**
	 * 翻译表字典文本
	 * @param fieldValue
	 * @param tableDict
	 * @param tableName
	 * @param cached
	 * @return
	 */
	private String translateTableDictVal(Object fieldValue, TableDict tableDict, TableName tableName, boolean cached) {
		String textValue;
		Map<String, Object> result =
				cached ? getTableDictValCache(fieldValue, tableDict, tableName) :
						getTableDictValNoCache(fieldValue, tableDict, tableName);
		textValue = String.valueOf(result.get(tableDict.val()));
		return textValue;
	}

	public Map<String, Object> getTableDictValNoCache(Object fieldValue, TableDict tableDict, TableName tableName) {
		return sqlMapper.dynamicsQuery("SELECT " + tableDict.val() + " FROM " + tableName.value() +
				" WHERE " + tableDict.key() + " = " + fieldValue);
	}

	@Cacheable(value = CommonCacheConstant.TABLE_DICT, condition = "!#result == null")
	public Map<String, Object> getTableDictValCache(Object fieldValue, TableDict tableDict, TableName tableName) {
		return getTableDictValNoCache(fieldValue, tableDict, tableName);
	}

	/**
	 * 翻译字典文本
	 * @param code
	 * @param key
	 * @return
	 */
	@Deprecated
	private String translateDictValue(String code, Object key, boolean cache) {
		String keyStr = JsonUtil.toJson(key);
		if (!StringUtils.hasText(keyStr)) {
			return null;
		}
		if (JsonUtil.isJsonObj(keyStr)) {
			log.warn("对象需要转字典的对应类型不能为对象");
			return null;
		}
		if (JsonUtil.isJsonArray(keyStr)) {
			log.warn("对象需要转字典的对应类型不能为可迭代对象");
			return null;
		}
		StringBuilder textValue = new StringBuilder();
		String[] keys = keyStr.split(",");
		for (String k : keys) {
			String tmpValue;
			if (k.trim().length() == 0) {
				continue; //跳过循环
			}
			tmpValue = cache ? dbDictService.queryDictTextByKey(code, k.trim()) : dbDictService.queryDictTextByKeyNoCache(code, k.trim());
			if (tmpValue != null) {
				if (!"".equals(textValue.toString())) {
					textValue.append(",");
				}
				textValue.append(tmpValue);
			}
		}
		return textValue.toString();
	}

	/**
	 * 字典信息
	 */
	@AllArgsConstructor
	private static class DbDictFieldHolder {

		/**
		 * 字典ID
		 */
		private final String dictId;

		/**
		 * field
		 */
		private final Field field;

		/**
		 * 值后缀
		 */
		private final String nameSuffix;

		/**
		 * 是否缓存
		 */
		private final boolean cached;
	}
}