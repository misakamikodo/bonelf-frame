package com.bonelf.frame.web.aop;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bonelf.cicada.util.EnumUtil;
import com.bonelf.frame.base.util.JsonUtil;
import com.bonelf.frame.core.dict.constraints.DbDict;
import com.bonelf.frame.core.dict.constraints.DictField;
import com.bonelf.frame.core.dict.constraints.EnumDict;
import com.bonelf.frame.web.core.dict.service.DbDictService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * 字典aop类
 * @author bonelf
 * @date 2020-10-27
 */
@Slf4j
@Deprecated
// @Aspect
// @Component
public class DictAspect {
	/**
	 * queryDictTextByKey的@Cacheable的Aop生效，否则也可以注入自己把方法写类里面，但是我不这么做
	 */
	@Autowired
	private DbDictService dbDictService;


	// 定义切点Pointcut
	@Pointcut("execution(public * com.bonelf..*.*Controller.*(..))")
	public void executeService() {
	}

	@Around("executeService()")
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		long time1 = System.currentTimeMillis();
		Object result = pjp.proceed();
		long time2 = System.currentTimeMillis();
		//log.debug("supportFeignClient获取数据 耗时：" + (time2 - time1) + "ms");
		long start = System.currentTimeMillis();
		if (result != null) {
			this.parseDictText(result);
		}
		long end = System.currentTimeMillis();
		//log.debug("解析注入数据  耗时" + (end - start) + "ms");
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
		for (Field field : fields) {
			Object fieldValue = ReflectUtil.getFieldValue(record, field.getName());
			if (fieldValue == null) {
				continue;
			}
			//对每个field进行判断
			DbDict dbDict = field.getAnnotation(DbDict.class);
			if (dbDict != null) {
				String code = dbDict.value();
				String nameSuffix = dbDict.nameSuffix();
				//翻译字典值对应的txt
				String textValue = translateDictValue(code, fieldValue, dbDict.cached());
				log.debug("字典Val : " + textValue);
				log.debug("翻译字典字段： " + field.getName() + nameSuffix + ":" + textValue);
				try {
					ReflectUtil.setFieldValue(record, field.getName() + nameSuffix, textValue);
				} catch (UtilException | IllegalArgumentException e) {
					log.warn("对象需要转字典的对应Field找不到：{}", field.getName() + nameSuffix);
				}
			}
			//对每个field进行判断
			EnumDict enumDict = field.getAnnotation(EnumDict.class);
			if (enumDict != null) {
				String nameSuffix = enumDict.nameSuffix();
				//翻译字典值对应的txt
				String textValue = EnumUtil.getEnumString(fieldValue, enumDict.value());
				log.debug("字典Val : " + textValue);
				log.debug("翻译字典字段： " + field.getName() + nameSuffix + ":" + textValue);
				try {
					ReflectUtil.setFieldValue(record, field.getName() + nameSuffix, textValue);
				} catch (UtilException | IllegalArgumentException e) {
					log.warn("对象需要转字典的对应Field找不到：{}，请检查类型名称是否正确和类型是否为String", field.getName() + nameSuffix);
				}
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
				} else {
					parseDictText(fieldValue);
				}
			}
		}
	}

	/**
	 * 翻译字典文本
	 * @param code
	 * @param key
	 * @return
	 */
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

}
