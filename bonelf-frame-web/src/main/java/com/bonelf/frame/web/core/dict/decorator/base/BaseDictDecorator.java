package com.bonelf.frame.web.core.dict.decorator.base;

/**
 * 基础装饰器
 * @author ccy
 * @date 2021/8/8 16:45
 */
public abstract class BaseDictDecorator<K, V> {

	protected K key;

	/**
	 * 装饰
	 * @param target
	 * @return
	 */
	protected abstract V decorate(K target);
}
