/*
 * Copyright (c) 2021. Bonelf.
 */

package com.bonelf.frame.web.core.advice;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bonelf.frame.base.util.JsonUtil;
import com.bonelf.frame.base.util.SpringContextUtils;
import com.bonelf.frame.core.dict.constraints.*;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.web.constant.ResultCostAttr;
import com.bonelf.frame.web.core.dict.decorator.*;
import com.bonelf.frame.web.core.dict.domain.BatchDictFieldHolder;
import com.bonelf.frame.web.core.dict.service.DbDictService;
import com.bonelf.frame.web.core.dict.service.RemoteDictService;
import com.bonelf.frame.web.core.dict.service.TableDictService;
import com.bonelf.frame.web.core.dict.service.impl.LocalDbDictServiceImpl;
import com.bonelf.frame.web.domain.SimplePageInfo;
import com.bonelf.frame.web.mapper.SysDictItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
	@Autowired(required = false)
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
	@Autowired(required = false)
	private TableDictService tableDictService;
	@Autowired(required = false)
	private RemoteDictService remoteDictService;

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
		Object bodyValue;
		if (body instanceof ResponseEntity) {
			bodyValue = ((ResponseEntity<?>)body).getBody();
		} else {
			bodyValue = body;
		}
		Result<?> result;
		if (bodyValue instanceof Result) {
			result = (Result<?>)bodyValue;
		} else {
			result = Result.ok(bodyValue);
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
		// 存储需要装饰的Field索引 如果遇到列表、分页对象 则遍历取同一字段（只取确定类型的列表List<?>和Map不支持注解翻译）
		List<BatchDictFieldHolder<RemoteDict>> remoteFields = new ArrayList<>();
		List<BatchDictFieldHolder<TableDict>> tableFields = new ArrayList<>();
		List<BatchDictFieldHolder<DbDict>> dbDictFields = new ArrayList<>();
		List<BatchDictFieldHolder<Annotation>> wrapperFields = new ArrayList<>();
		this.parseDictText(result, remoteFields, tableFields, dbDictFields, wrapperFields);
		// 翻译数据库字典
		if (!dbDictFields.isEmpty()) {
			new DbDictDecorator(dbDictService, dbDictFields).decorate();
		}
		if (!tableFields.isEmpty()) {
			new TableDictDecorator(tableDictService, tableFields).decorate();
		}
		if (!remoteFields.isEmpty()) {
			new RemoteDictDecorator(remoteDictService, remoteFields).decorate();
		}
		if (!wrapperFields.isEmpty()) {
			new DictWrapperDictDecorator(dbDictService,
					tableDictService,
					remoteDictService, wrapperFields).decorate();
		}
		long end = System.currentTimeMillis();
		return result;
	}


	/**
	 * 本方法针对返回对象为Result 的IPage的分页列表数据进行动态字典注入
	 * 例输入当前返回值的就会多出一个sex_{nameSuffix}字段
	 * @param record
	 */
	private <T> void parseDictText(@NonNull T record,
								   List<BatchDictFieldHolder<RemoteDict>> remoteFields,
								   List<BatchDictFieldHolder<TableDict>> tableFields,
								   List<BatchDictFieldHolder<DbDict>> dbDictFields,
								   List<BatchDictFieldHolder<Annotation>> wrapperFields) {
		//对POJO解析
		Field[] fields = ReflectUtil.getFields(record.getClass());
		if (ArrayUtil.isEmpty(fields)) {
			return;
		}
		for (Field field : fields) {
			Object fieldValue = ReflectUtil.getFieldValue(record, field.getName());
			if (fieldValue == null) {
				continue;
			}
			String recordStr = JsonUtil.toJson(fieldValue);
			// 是否是简单对象
			boolean pojoAndClt = JsonUtil.isJsonObj(recordStr) || JsonUtil.isJsonArray(recordStr);
			// 如果被字典对象注释，则record 视为序列化的对象 不可序列化的报错
			if (!pojoAndClt) {
				DbDict dbDict = field.getAnnotation(DbDict.class);
				if (dbDict != null) {
					dbDictFields.add(new BatchDictFieldHolder<>(record, field, dbDict));
				}
				EnumDict enumDict = field.getAnnotation(EnumDict.class);
				if (enumDict != null) {
					new EnumDictDecorator(record, field, enumDict).decorate();
				}
				TableDict tableDict = field.getAnnotation(TableDict.class);
				if (tableDict != null) {
					tableFields.add(new BatchDictFieldHolder<>(record, field, tableDict));
				}
				FuncDict funcDict = field.getAnnotation(FuncDict.class);
				if (funcDict != null) {
					new FuncDictDecorator(record, field, funcDict).decorate();
				}
				RemoteDict remoteDict = field.getAnnotation(RemoteDict.class);
				if (remoteDict != null) {
					remoteFields.add(new BatchDictFieldHolder<>(record, field, remoteDict));
				}
			} else {
				// 被此注解注释的为 Map Collection<?> 对象，使用协议化字符串判断
				DictWrappers dictWrappers = field.getAnnotation(DictWrappers.class);
				if (dictWrappers != null) {
					wrapperFields.add(new BatchDictFieldHolder<>(record, field, dictWrappers));
				}
				DictWrapper dictWrapper = field.getAnnotation(DictWrapper.class);
				if (dictWrapper != null) {
					wrapperFields.add(new BatchDictFieldHolder<>(record, field, dictWrapper));
				}
			}
			// 被此注解标记的为复杂对象和列表分页对象（分页对象也可作为列表对象处理，这里简化操作进行特殊化）
			DictField dictField = field.getAnnotation(DictField.class);
			if (dictField != null) {
				if (dictField.getClass() == record.getClass()) {
					log.error("DictField添加的属性Class类型和当前类相同将引起StackOverflowError，" +
							"不予自动装配字典，请手动添加子属性的字典值");
					return;
				}
				if (!pojoAndClt) {
					// 不解析String、int等等非 POJO
					return;
				}
				if (fieldValue instanceof Collection) {
					Collection<?> collection = (Collection<?>)fieldValue;
					for (Object c : collection) {
						parseDictText(c, remoteFields, tableFields, dbDictFields, wrapperFields);
					}
					return;
				} else if (fieldValue.getClass().isArray()) {
					Object[] objects = (Object[])fieldValue;
					for (Object c : objects) {
						parseDictText(c, remoteFields, tableFields, dbDictFields, wrapperFields);
					}
					return;
				} else if (fieldValue instanceof IPage) {
					IPage<?> page = (IPage<?>)fieldValue;
					for (Object c : page.getRecords()) {
						parseDictText(c, remoteFields, tableFields, dbDictFields, wrapperFields);
					}
					return;
				} else if (fieldValue instanceof SimplePageInfo) {
					SimplePageInfo<?> page = (SimplePageInfo<?>)fieldValue;
					for (Object c : page.getRecords()) {
						parseDictText(c, remoteFields, tableFields, dbDictFields, wrapperFields);
					}
					return;
				} else if (fieldValue instanceof Map) {
					// Map 跳过
				} else {
					// 简单对象
					parseDictText(fieldValue, remoteFields, tableFields, dbDictFields, wrapperFields);
				}
			}
		}
	}
}