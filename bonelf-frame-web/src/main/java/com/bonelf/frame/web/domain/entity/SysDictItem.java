package com.bonelf.frame.web.domain.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.bonelf.frame.web.domain.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典ID
 * @author bonelf
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_dict_item")
public class SysDictItem extends BaseEntity {

	/**
	 * 字典ID
	 */
	private String id;
	/**
	 * 字典ID
	 */
	private String dictId;
	/**
	 * itemText
	 */
	private String itemText;
	/**
	 * itemValue
	 */
	private String itemValue;
	/**
	 * description
	 */
	private String description;
	/**
	 * 排序 升序
	 */
	private Long sortOrder;
	/**
	 * 状态
	 * @see com.bonelf.frame.web.constant.SysDictItemStatusEnum
	 */
	private Long itemStatus;

}
