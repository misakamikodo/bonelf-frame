package com.bonelf.frame.web.core.dict.decorator;

import cn.hutool.core.util.StrUtil;
import com.bonelf.frame.core.dict.annotation.RemoteDict;
import com.bonelf.frame.web.core.dict.decorator.base.BaseBatchDictDecorator;
import com.bonelf.frame.web.core.dict.domain.BatchDictFieldHolder;
import com.bonelf.frame.web.core.dict.domain.RemoteDictValue;
import com.bonelf.frame.web.core.dict.service.RemoteDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 远程接口字典
 * @author bonelf
 * @date 2021/8/11 14:15
 */
@Slf4j
public class RemoteDictDecorator extends BaseBatchDictDecorator<RemoteDict> {
	private static final String DEFAULT_ADDR =
			"{url}{conn}{queryKey}={values}";

	private final RemoteDictService remoteDictService;

	public RemoteDictDecorator(RemoteDictService remoteDictService, List<BatchDictFieldHolder<RemoteDict>> dictFields) {
		super(dictFields);
		this.remoteDictService = remoteDictService;
	}

	@Override
	public void decorate() {
		Set<RemoteDictValue> cacheQuery = new HashSet<>();
		Set<RemoteDictValue> noCacheQuery = new HashSet<>();
		for (BatchDictFieldHolder<RemoteDict> entry : dictFields) {
			Object fieldValue = entry.getFieldValue();
			RemoteDictValue item = new RemoteDictValue();
			item.setItemValue(fieldValue);
			String toMapMethod = entry.getAnnotation() == null ?
					(String)entry.getAnnotationValues().get("toMapMethod") :
					entry.getAnnotation().toMapMethod();
			if (StrUtil.isNotBlank(toMapMethod)) {
				String[] clzMethod = toMapMethod.split("#");
				if (clzMethod.length < 1) {
					throw new IllegalArgumentException("请配置正确的方法 exp: com.example.Func#foo");
				}
				item.setMethodClz(clzMethod[0]);
				item.setMethodName(clzMethod[1]);
			}
			RemoteDict remoteDict = entry.getAnnotation();
			boolean cached = remoteDict == null ?
					(boolean)entry.getAnnotationValues().get("cached") :
					remoteDict.cached();
			item.setAddr(getAddrFromAnnotation(remoteDict, entry.getAnnotationValues()));
			if (cached) {
				cacheQuery.add(item);
			} else {
				noCacheQuery.add(item);
			}
		}
		if (!noCacheQuery.isEmpty()) {
			Map<RemoteDictValue, String> resNoCached = remoteDictService.queryDictTextByKeyNoCache(noCacheQuery);
			wrapDictValue2Field(dictFields, resNoCached);
		}
		if (!cacheQuery.isEmpty()) {
			Map<RemoteDictValue, String> resCached = remoteDictService.queryDictTextByKey(cacheQuery);
			wrapDictValue2Field(dictFields, resCached);
		}
	}

	/**
	 * 从注解中解析sql模板
	 * @param remoteDict
	 * @param annotationValues
	 * @return
	 */
	private String getAddrFromAnnotation(RemoteDict remoteDict, Map<String, Object> annotationValues) {
		String url = remoteDict == null ? (String)annotationValues.get("value") :
				remoteDict.value();
		// ExpressionParser parser = new SpelExpressionParser();
		// TemplateParserContext parserContext = new TemplateParserContext();
		// Map<String, Object> params = new HashMap<>();
		// params.put("url", url);
		// String content = parser.parseExpression(DEFAULT_ADDR, parserContext).getValue(params, String.class);
		return DEFAULT_ADDR
				.replace("{url}", url)
				.replace("{conn}", url.contains("?") ? "&" : "?")
				.replace("{queryKey}", remoteDict == null ? (String)annotationValues.get("queryKey"):
						remoteDict.queryKey()
				);
	}

	/**
	 * 封装字典信息到对象
	 * @param dictIdFieldList
	 * @param resCached
	 */
	protected void wrapDictValue2Field(List<BatchDictFieldHolder<RemoteDict>> dictIdFieldList,
									   Map<RemoteDictValue, String> resCached) {
		Map<String, List<BatchDictFieldHolder<RemoteDict>>> group = dictIdFieldList.stream().collect(
				Collectors.groupingBy(item -> getAddrFromAnnotation(item.getAnnotation(),
						item.getAnnotationValues()))
		);
		for (Map.Entry<RemoteDictValue, String> entry : resCached.entrySet()) {
			String textValue = entry.getValue();
			String sql = entry.getKey().getAddr();
			if (!group.containsKey(sql)) {
				continue;
			}
			for (BatchDictFieldHolder<RemoteDict> holder : group.get(sql)) {
				String nameSuffix = getNameSuffix(holder);
				holder.setFieldValue(nameSuffix, textValue);
			}
		}
	}

	protected String getNameSuffix(BatchDictFieldHolder<RemoteDict> holder) {
		if (holder.getField() != null) {
			return holder.getAnnotation().nameSuffix();
		} else {
			return (String)holder.getAnnotationValues().get("nameSuffix");
		}
	}
}
