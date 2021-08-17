package com.bonelf.frame.web.core.dict.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 远程查询(查询对象)
 * @author bonelf
 * @date 2021/6/15 11:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteDictValue implements Serializable {
	/**
	 * 查询地址
	 */
	private String addr;

	private String methodClz;

	private String methodName;

	private Object itemValue;
}
