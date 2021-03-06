/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.frame.web.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * 手机验证码登录Token认证类
 */
@Getter
@Setter
public abstract class BaseApiAuthenticationToken extends UsernamePasswordAuthenticationToken {
    /**
     * 在下面添加自定义内容 (认证通过后添加)
     * 认证服务：com.bonelf.auth.core.oauth2.converter.CustomTokenEnhancer
     */
    private Object userId;

    public BaseApiAuthenticationToken(Authentication authenticationToken) {
        super(authenticationToken.getPrincipal(), authenticationToken.getCredentials());
    }


}
