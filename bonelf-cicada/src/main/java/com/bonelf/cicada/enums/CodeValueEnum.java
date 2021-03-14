package com.bonelf.cicada.enums;

import java.io.Serializable;

/**
 * 注释：枚举接口 （规范）
 * @Author: caiyuan
 * @Date: 2020/8/6 0006 9:22
 * @Version: v1.FreezeEnum
 */
public interface CodeValueEnum<T extends Serializable> {

	/**
	 * 字典code
	 * @return
	 */
	T getCode();

	/**
	 * 字典name
	 * @return
	 */
	String getValue();
}
