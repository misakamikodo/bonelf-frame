package com.bonelf.frame.web.core.dict.decorator;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bonelf.frame.core.dict.constraints.TableDict;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.web.core.dict.decorator.base.BaseBatchDictDecorator;
import com.bonelf.frame.web.core.dict.domain.BatchDictFieldHolder;
import com.bonelf.frame.web.core.dict.domain.TableDictValue;
import com.bonelf.frame.web.core.dict.service.TableDictService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表字典装饰器
 * @author bonelf
 * @date 2021/8/11 14:15
 */
@Slf4j
public class TableDictDecorator extends BaseBatchDictDecorator<TableDict> {
	private static final String DEFAULT_SQL =
			"SELECT :key `key`, :val `value` FROM :table WHERE :key IN (:values)";

	private final TableDictService tableDictService;

	public TableDictDecorator(TableDictService tableDictService, List<BatchDictFieldHolder<TableDict>> dictFields) {
		super(dictFields);
		this.tableDictService = tableDictService;
	}

	@Override
	public void decorate() {
		Set<TableDictValue> cacheQuery = new HashSet<>();
		Set<TableDictValue> noCacheQuery = new HashSet<>();
		for (BatchDictFieldHolder<TableDict> entry : dictFields) {
			Object fieldValue = entry.getFieldValue();
			TableDictValue item = new TableDictValue();
			item.setItemValue(fieldValue);
			TableDict tableDict = entry.getAnnotation();
			boolean cached = tableDict == null ?
					(boolean)entry.getAnnotationValues().get("cached") :
					tableDict.cached();
			item.setSql(getSqlFromAnnotation(tableDict, entry.getAnnotationValues()));
			if (cached) {
				cacheQuery.add(item);
			} else {
				noCacheQuery.add(item);
			}
		}
		if (!noCacheQuery.isEmpty()) {
			Map<TableDictValue, String> resNoCached = tableDictService.queryDictTextByKeyNoCache(noCacheQuery);
			wrapDictValue2Field(dictFields, resNoCached);
		}
		if (!cacheQuery.isEmpty()) {
			Map<TableDictValue, String> resCached = tableDictService.queryDictTextByKey(cacheQuery);
			wrapDictValue2Field(dictFields, resCached);
		}
	}

	/**
	 * 从注解中解析sql模板
	 * @param tableDict
	 * @param annotationValues
	 * @return
	 */
	private String getSqlFromAnnotation(TableDict tableDict, Map<String, Object> annotationValues) {
		Class<?> table;
		try {
			table = tableDict == null ?
					ClassLoader.getSystemClassLoader().loadClass((String)annotationValues.get("value")) :
					tableDict.value();
		} catch (ClassNotFoundException e) {
			throw new BonelfException(e);
		}
		String sql = tableDict == null ?
				(String)annotationValues.get("sql") :
				tableDict.sql();
		return StrUtil.isBlank(sql) ?
				DEFAULT_SQL
						.replace(":key", tableDict == null ?
								(String)annotationValues.get("key") :
								tableDict.key())
						.replace(":val ", (tableDict == null ?
								(String)annotationValues.get("val") :
								tableDict.val()) + " ")
						.replace(":table", table.getAnnotation(TableName.class).value())
				:
				sql;
	}

	/**
	 * 封装字典信息到对象
	 * @param dictIdFieldList
	 * @param resCached
	 */
	protected void wrapDictValue2Field(List<BatchDictFieldHolder<TableDict>> dictIdFieldList,
									   Map<TableDictValue, String> resCached) {
		Map<String, List<BatchDictFieldHolder<TableDict>>> group = dictIdFieldList.stream().collect(
				Collectors.groupingBy(item ->
						getSqlFromAnnotation(item.getAnnotation(), item.getAnnotationValues()))
		);
		for (Map.Entry<TableDictValue, String> entry : resCached.entrySet()) {
			String textValue = entry.getValue();
			String sql = entry.getKey().getSql();
			if (!group.containsKey(sql)) {
				continue;
			}
			// 同一张表
			for (BatchDictFieldHolder<TableDict> holder : group.get(sql)) {
				String nameSuffix = getNameSuffix(holder);
				holder.setFieldValue(nameSuffix, textValue);
			}
		}
	}

	protected String getNameSuffix(BatchDictFieldHolder<TableDict> holder) {
		if (holder.getField() != null) {
			return holder.getAnnotation().nameSuffix();
		} else {
			return (String)holder.getAnnotationValues().get("nameSuffix");
		}
	}
}
