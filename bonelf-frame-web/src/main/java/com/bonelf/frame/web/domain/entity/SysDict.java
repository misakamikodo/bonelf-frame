package com.bonelf.frame.web.domain.entity;

import com.bonelf.frame.web.domain.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * sys_dict
 * @author 
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysDict extends BaseEntity {
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