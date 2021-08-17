package com.bonelf.frame.web.core.dict.domain;

import lombok.Data;

/**
 * 字典查询(返回对象)
 * @author bonelf
 * @date 2021/6/15 11:38
 */
@Data
public class DbDictText {
	/**
	 * 字典ID
	 */
	private String dictId;
	/**
	 * 字典键值
	 */
	private String itemValue;
	/**
	 * 字典翻译后文本
	 */
	private String itemText;
}
