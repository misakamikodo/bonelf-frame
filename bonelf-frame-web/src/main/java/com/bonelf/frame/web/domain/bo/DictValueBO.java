package com.bonelf.frame.web.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典查询
 * @author ccy
 * @date 2021/6/15 11:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictValueBO {

	private String dictId;

	private Object itemValue;
}
