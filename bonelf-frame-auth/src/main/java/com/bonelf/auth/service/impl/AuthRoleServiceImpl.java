package com.bonelf.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.bonelf.frame.core.auth.domain.Role;
import com.bonelf.frame.core.auth.service.AuthRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthRoleServiceImpl implements AuthRoleService {

    @Override
    public Set<Role> queryUserRolesByUserId(Long userId) {
        Role exampleRoleResponse = new Role();
        exampleRoleResponse.setCode("test:authority");
        exampleRoleResponse.setName("testAuthority");
        exampleRoleResponse.setDescription("this is an example authority");
        Role exampleRoleResponse2 = new Role();
        exampleRoleResponse2.setCode("ROLE_role");
        exampleRoleResponse2.setName("testRole");
        exampleRoleResponse2.setDescription("this is an example role");
        return CollectionUtil.newHashSet(exampleRoleResponse, exampleRoleResponse2);
    }

}
