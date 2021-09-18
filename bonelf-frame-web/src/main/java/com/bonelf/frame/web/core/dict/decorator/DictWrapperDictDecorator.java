package com.bonelf.frame.web.core.dict.decorator;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bonelf.frame.base.util.JsonUtil;
import com.bonelf.frame.core.dict.constraints.*;
import com.bonelf.frame.web.core.dict.decorator.base.BaseBatchDictDecorator;
import com.bonelf.frame.web.core.dict.domain.BatchDictFieldHolder;
import com.bonelf.frame.web.core.dict.service.DbDictService;
import com.bonelf.frame.web.core.dict.service.RemoteDictService;
import com.bonelf.frame.web.core.dict.service.TableDictService;
import com.bonelf.frame.web.domain.SimplePageInfo;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 表字典装饰器
 * @author bonelf
 * @date 2021/8/11 14:15
 */
@Slf4j
public class DictWrapperDictDecorator extends BaseBatchDictDecorator<Annotation> {

	private final DbDictService dbDictService;

	private final TableDictService tableDictService;

	private final RemoteDictService remoteDictService;

	private List<BatchDictFieldHolder<RemoteDict>> remoteFields = new ArrayList<>();

	private List<BatchDictFieldHolder<TableDict>> tableFields = new ArrayList<>();

	private List<BatchDictFieldHolder<DbDict>> dbDictFields = new ArrayList<>();

	public DictWrapperDictDecorator(DbDictService dbDictService,
									TableDictService tableDictService,
									RemoteDictService remoteDictService,
									List<BatchDictFieldHolder<Annotation>> dictFields) {
		super(dictFields);
		this.dbDictService = dbDictService;
		this.tableDictService = tableDictService;
		this.remoteDictService = remoteDictService;
	}

	@Override
	public void decorate() {
		for (BatchDictFieldHolder<Annotation> dictField : dictFields) {
			if (dictField.getAnnotation() instanceof DictWrappers) {
				DictWrappers dictWrappers = (DictWrappers)dictField.getAnnotation();
				for (DictWrapper dictWrapper : dictWrappers.value()) {
					String[] fieldArr = dictWrapper.fieldSeq().split("\\.");
					if (dictField.getField() != null) {
						String curField = getDictFieldFieldName(dictField);
						fieldArr = ArrayUtil.insert(fieldArr, 0, curField);
					}
					parseDictText(dictField.getTarget(), fieldArr,
							dictWrapper);
				}
			} else if (dictField.getAnnotation() instanceof DictWrapper) {
				DictWrapper dictWrapper = (DictWrapper)dictField.getAnnotation();
				String[] fieldArr = dictWrapper.fieldSeq().split("\\.");
				if (dictField.getField() != null) {
					String curField = getDictFieldFieldName(dictField);
					fieldArr = ArrayUtil.insert(fieldArr, 0, curField);
				}
				parseDictText(dictField.getTarget(), fieldArr,
						dictWrapper);
			}
		}
		if (remoteFields.size() > 0) {
			new RemoteDictDecorator(remoteDictService, remoteFields).decorate();
		}
		if (tableFields.size() > 0) {
			new TableDictDecorator(tableDictService, tableFields).decorate();
		}
		if (dbDictFields.size() > 0) {
			new DbDictDecorator(dbDictService, dbDictFields).decorate();
		}
	}

	private String getDictFieldFieldName(BatchDictFieldHolder<Annotation> dictField) {
		if (dictField.getField() == null) {
			return dictField.getMapField();
		} else {
			return dictField.getField().getName();
		}
	}

	/**
	 * 翻译字典处理
	 * @param target
	 * @param fieldArr    从 target 对象开始的需要翻译的 field
	 * @param dictWrapper
	 */
	private void parseDictText(Object target,
							   String[] fieldArr,
							   DictWrapper dictWrapper) {
		if (fieldArr.length == 0) {
			log.warn("找不到" + Arrays.toString(fieldArr));
			return;
		}
		Object fieldValue = target;
		String[] newFieldArr = fieldArr;
		String recordStr = JsonUtil.toJson(fieldValue);
		// 是否是简单对象
		boolean pojo = JsonUtil.isJsonObj(recordStr);
		if (fieldValue instanceof Collection) {
			Collection<?> collection = (Collection<?>)fieldValue;
			for (Object c : collection) {
				parseDictText(c, newFieldArr, dictWrapper);
			}
		} else if (fieldValue.getClass().isArray()) {
			Object[] objects = (Object[])fieldValue;
			for (Object c : objects) {
				parseDictText(c, newFieldArr, dictWrapper);
			}
		} else if (fieldValue instanceof IPage) {
			IPage<?> page = (IPage<?>)fieldValue;
			for (Object c : page.getRecords()) {
				parseDictText(c, newFieldArr, dictWrapper);
			}
		} else if (fieldValue instanceof SimplePageInfo) {
			SimplePageInfo<?> page = (SimplePageInfo<?>)fieldValue;
			for (Object c : page.getRecords()) {
				parseDictText(c, newFieldArr, dictWrapper);
			}
		} else if (pojo && fieldArr.length > 1) {
			// 翻译方法
			if (target instanceof Map) {
				fieldValue = ((Map<String, Object>)target).get(fieldArr[0]);
			} else {
				fieldValue = ReflectUtil.getFieldValue(target, fieldArr[0]);
			}
			newFieldArr = ArrayUtil.remove(fieldArr, 0);
			parseDictText(fieldValue, newFieldArr, dictWrapper);
		} else {
			parseFieldtext(fieldValue, fieldArr[0], dictWrapper);
		}
	}

	private void parseFieldtext(Object target, String fieldName, DictWrapper dictWrapper) {
		Field field = null;
		if (target instanceof Map) {
			field = ReflectUtil.getField(target.getClass(), fieldName);
		}
		String payloadStr = dictWrapper.payload();
		String[] payloadArr = payloadStr.split(";");
		Map<String, Object> payloadMap = new HashMap<>();
		payloadMap.put("nameSuffix", dictWrapper.nameSuffix());
		payloadMap.put("cached", dictWrapper.cached());
		for (String payload : payloadArr) {
			String[] keyVal = payload.split("=");
			if (keyVal.length < 2) {
				continue;
			}
			payloadMap.put(keyVal[0], keyVal[1]);
		}
		switch (dictWrapper.type()) {
			case db:
				if (field != null) {
					dbDictFields.add(new BatchDictFieldHolder<>(target, field, payloadMap));
				} else {
					dbDictFields.add(new BatchDictFieldHolder<>(target, fieldName, payloadMap));
				}
				break;
			case table:
				if (field != null) {
					tableFields.add(new BatchDictFieldHolder<>(target, field, payloadMap));
				} else {
					tableFields.add(new BatchDictFieldHolder<>(target, fieldName, payloadMap));
				}
				break;
			case remote:
				if (field != null) {
					remoteFields.add(new BatchDictFieldHolder<>(target, field, payloadMap));
				} else {
					remoteFields.add(new BatchDictFieldHolder<>(target, fieldName, payloadMap));
				}
				break;
			case func:
				if (field != null) {
					new FuncDictDecorator(target, field, payloadMap).decorate();
				} else {
					new FuncDictDecorator(target, fieldName, payloadMap).decorate();
				}
				break;
			case enums:
				if (field != null) {
					new EnumDictDecorator(target, field, payloadMap).decorate();
				} else {
					new EnumDictDecorator(target, fieldName, payloadMap).decorate();
				}
				break;
			default:
		}
	}
}
