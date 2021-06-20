package com.bonelf.support.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bonelf.frame.web.domain.entity.SysDict;
import com.bonelf.frame.web.mapper.SysDictMapper;
import com.bonelf.support.web.service.DictService;
import org.springframework.stereotype.Service;

@Service
public class DictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements DictService {
}
