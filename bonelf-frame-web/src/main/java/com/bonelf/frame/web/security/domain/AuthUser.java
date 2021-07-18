package com.bonelf.frame.web.security.domain;

import com.bonelf.frame.core.constant.UsernameType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 扩展认证用户 添加用户ID
 * <p>
 * 见{@link org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider#authenticate(Authentication) 认证} 192
 * 需要设置 forcePrincipalAsString false
 * {@link org.springframework.security.authentication.AbstractAuthenticationToken#getName() 获取用户名}
 * @author ccy
 * @date 2021/5/21 10:27
 * @see com.bonelf.auth.core.oauth2.granter.base.BaseApiAuthenticationProvider 将用户ID放到authentication中返回
 */
public class AuthUser extends org.springframework.security.core.userdetails.User {
	@Getter
	@Setter
	private Long userId;
	@Getter
	@Setter
	private UsernameType usernameType;
	// @Getter
	// @Setter
	// private String uniqueId;

	public AuthUser(String username, UsernameType idType, String password) {
		super(username, password, new ArrayList<>());
		this.usernameType = idType;
	}

	public AuthUser(String username, UsernameType idType, String password, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
		this.usernameType = idType;
	}

	public AuthUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
	}

	public AuthUser(Long userId, UsernameType idType, String uniqueId,
					String password, boolean enabled, boolean accountNonExpired,
					boolean credentialsNonExpired, boolean accountNonLocked,
					Collection<? extends GrantedAuthority> authorities) {
		super(uniqueId,
				password, enabled, accountNonExpired,
				credentialsNonExpired, accountNonLocked,
				authorities);
		this.userId = userId;
		this.usernameType = idType;
		// this.uniqueId = uniqueId;
	}
}
