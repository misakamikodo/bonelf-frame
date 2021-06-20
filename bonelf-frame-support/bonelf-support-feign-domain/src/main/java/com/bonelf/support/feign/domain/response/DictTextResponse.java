package com.bonelf.support.feign.domain.response;

import lombok.Data;

/**
 * 字典查询
 * @author ccy
 * @date 2021/6/15 11:38
 */
@Data
public class DictTextResponse {

	private String dictId;

	private String itemValue;

	private String itemText;
}
