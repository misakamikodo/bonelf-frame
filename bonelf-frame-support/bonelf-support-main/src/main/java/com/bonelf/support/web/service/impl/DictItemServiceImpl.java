package com.bonelf.support.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bonelf.support.web.domain.entity.DictItem;
import com.bonelf.support.web.mapper.DictItemMapper;
import com.bonelf.support.web.service.DictItemService;
import org.springframework.stereotype.Service;

@Service
public class DictItemServiceImpl extends ServiceImpl<DictItemMapper, DictItem> implements DictItemService {

}
