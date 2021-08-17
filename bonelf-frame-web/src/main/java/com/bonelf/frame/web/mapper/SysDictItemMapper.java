package com.bonelf.frame.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bonelf.frame.web.core.dict.domain.DbDictText;
import com.bonelf.frame.web.core.dict.domain.DbDictValue;
import com.bonelf.frame.web.domain.entity.SysDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

@Mapper
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {
	/**
	 * 根据字典键值查询字典文本
	 * @param dictId
	 * @param itemValue
	 * @return
	 */
	String selectDictTextByItemValue(@Param("dictId") String dictId, @Param("itemValue") String itemValue);

	/**
	 * 批量根据字典键值查询字典文本
	 * @param dictText
	 * @return
	 */
	Set<DbDictText> selectDictTextByItemValueBatch(@Param("dictText") Set<DbDictValue> dictText);
}