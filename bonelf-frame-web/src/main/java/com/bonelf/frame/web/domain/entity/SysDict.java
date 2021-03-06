package com.bonelf.frame.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bonelf.frame.web.domain.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * sys_dict
 * @author 
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_dict")
public class SysDict extends BaseEntity {
    @TableId
    private String dictId;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典编码
     */
    private String dictCode;

    /**
     * 描述
     */
    private String description;
}