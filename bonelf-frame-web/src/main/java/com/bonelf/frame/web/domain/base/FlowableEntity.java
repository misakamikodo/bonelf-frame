package com.bonelf.frame.web.domain.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流实体
 * @author bonelf
 * @date 2020/4/22 0022 13:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class FlowableEntity extends BaseEntity {

	// private String processKey;
	/**
	 * 流程编号
	 */
	@TableField(value = "process_id", fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_NULL)
	private String processId;

	/**
	 * 流程状态
	 */
	@TableField(value = "process_status", fill = FieldFill.UPDATE, updateStrategy = FieldStrategy.NOT_NULL)
	private String processStatus;

}
