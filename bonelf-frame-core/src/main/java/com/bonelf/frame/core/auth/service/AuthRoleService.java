package com.bonelf.frame.core.auth.service;

import com.bonelf.frame.core.auth.domain.Role;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * <p>
 * 签权服务
 * </p>
 * @author bonelf
 * @since 2020/11/17 15:37
 */
public interface AuthRoleService {

    Set<Role> queryUserRolesByUserId(Long userId);

}
