package com.bonelf.frame.web.core.dict.decorator.base;

import com.bonelf.frame.web.core.dict.domain.BatchDictFieldHolder;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * 需要批量操作的基础字典装饰器
 * 传入一个键Set， 返回对应的值
 * @author bonelf
 * @date 2021/8/8 16:45
 */
@Slf4j
public abstract class BaseBatchDictDecorator<A extends Annotation> {

	protected List<BatchDictFieldHolder<A>> dictFields;

	protected BaseBatchDictDecorator(List<BatchDictFieldHolder<A>> dictIdFieldList) {
		this.dictFields = dictIdFieldList;
	}

	/**
	 * 装饰
	 * @return
	 */
	protected abstract void decorate();
}
