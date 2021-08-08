package com.bonelf.frame.web.core.dict.decorator.base;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 需要批量操作的基础字典装饰器
 * 传入一个键Set， 返回对应的值
 * @author ccy
 * @date 2021/8/8 16:45
 */
public abstract class BaseBatchDictDecorator<K extends Set<? extends Serializable>, V extends Map<?, ?>>
		extends BaseDictDecorator<K, V> {
}
