package com.bonelf.frame.web.core.dict.decorator;


import com.bonelf.frame.core.dict.annotation.DbDict;
import com.bonelf.frame.web.core.dict.decorator.base.BaseBatchDictDecorator;
import com.bonelf.frame.web.core.dict.domain.BatchDictFieldHolder;
import com.bonelf.frame.web.core.dict.domain.DbDictValue;
import com.bonelf.frame.web.core.dict.service.DbDictService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字典表字典装饰
 * @author bonelf
 * @date 2021/8/11 14:40
 */
@Slf4j
public class DbDictDecorator extends BaseBatchDictDecorator<DbDict> {
	/**
	 * 字典服务
	 */
	private final DbDictService dbDictService;

	public DbDictDecorator(DbDictService dbDictService, List<BatchDictFieldHolder<DbDict>> dictFields) {
		super(dictFields);
		this.dbDictService = dbDictService;
	}

	@Override
	public void decorate() {
		Set<DbDictValue> cacheQuery = new HashSet<>();
		Set<DbDictValue> noCacheQuery = new HashSet<>();
		for (BatchDictFieldHolder<DbDict> entry : dictFields) {
			Object fieldValue = entry.getFieldValue();
			DbDictValue item = new DbDictValue();
			item.setItemValue(fieldValue);
			DbDict dbDict = entry.getAnnotation();
			item.setDictId(dbDict == null ?
					(String)entry.getAnnotationValues().get("value") :
					dbDict.value());
			boolean cached = dbDict == null ?
					(boolean)entry.getAnnotationValues().get("cached") :
					dbDict.cached();
			if (cached) {
				cacheQuery.add(item);
			} else {
				noCacheQuery.add(item);
			}
		}
		if (!noCacheQuery.isEmpty()) {
			Map<DbDictValue, String> resNoCached = dbDictService.queryDictTextByKeyNoCache(noCacheQuery);
			wrapDbDictValue2Field(dictFields, resNoCached);
		}
		if (!cacheQuery.isEmpty()) {
			Map<DbDictValue, String> resCached = dbDictService.queryDictTextByKey(cacheQuery);
			wrapDbDictValue2Field(dictFields, resCached);
		}
	}


	/**
	 * 封装字典信息到对象
	 * @param dictIdFieldList
	 * @param resCached
	 */
	protected void wrapDbDictValue2Field(List<BatchDictFieldHolder<DbDict>> dictIdFieldList,
										 Map<DbDictValue, String> resCached) {
		Map<String, List<BatchDictFieldHolder<DbDict>>> group = dictIdFieldList.stream().collect(
				Collectors.groupingBy(item -> item.getAnnotation() == null ?
						(String)item.getAnnotationValues().get("value") :
						item.getAnnotation().value())
		);
		for (Map.Entry<DbDictValue, String> entry : resCached.entrySet()) {
			String textValue = entry.getValue();
			String dictId = entry.getKey().getDictId();
			if (!group.containsKey(dictId)) {
				continue;
			}
			for (BatchDictFieldHolder<DbDict> holder : group.get(dictId)) {
				String nameSuffix = getNameSuffix(holder);
				holder.setFieldValue(nameSuffix, textValue);
			}
		}
	}

	protected String getNameSuffix(BatchDictFieldHolder<DbDict> holder) {
		if (holder.getField() != null) {
			return holder.getAnnotation().nameSuffix();
		} else {
			return (String)holder.getAnnotationValues().get("nameSuffix");
		}
	}
}
