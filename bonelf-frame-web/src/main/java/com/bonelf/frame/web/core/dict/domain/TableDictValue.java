package com.bonelf.frame.web.core.dict.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 字典查询(查询对象)
 * @author bonelf
 * @date 2021/6/15 11:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableDictValue implements Serializable {
	/**
	 * 查询SQL
	 */
	private String sql;

	private Object itemValue;
}
