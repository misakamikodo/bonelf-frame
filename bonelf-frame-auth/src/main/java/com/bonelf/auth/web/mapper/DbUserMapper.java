package com.bonelf.auth.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bonelf.auth.web.domain.entity.DbUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DbUserMapper extends BaseMapper<DbUser> {
	DbUser selectOneByPhone(String username);
}