package com.bonelf.support.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bonelf.frame.web.core.dict.domain.DbDictValue;
import com.bonelf.frame.web.domain.entity.SysDictItem;
import com.bonelf.frame.web.mapper.SysDictItemMapper;
import com.bonelf.support.feign.domain.request.DictValueRequest;
import com.bonelf.support.feign.domain.response.DictTextResponse;
import com.bonelf.support.web.service.DictItemService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements DictItemService {

	@Override
	public String getTextByValue(String dictId, String itemValue) {
		return baseMapper.selectDictTextByItemValue(dictId, itemValue);
	}

	@Override
	public Set<DictTextResponse> getTextByValueBatch(Set<DictValueRequest> query) {
		return baseMapper.selectDictTextByItemValueBatch(query.stream().map(item -> {
			DbDictValue dictValue = new DbDictValue();
			dictValue.setDictId(item.getDictId());
			dictValue.setItemValue(item.getItemValue());
			return dictValue;
		}).collect(Collectors.toSet())).stream().map(item -> {
			DictTextResponse res = new DictTextResponse();
			res.setDictId(item.getDictId());
			res.setItemValue(item.getItemValue());
			res.setItemText(item.getItemText());
			return res;
		}).collect(Collectors.toSet());
	}
}
