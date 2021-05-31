package com.bonelf.frame.web.security.converter;

import com.bonelf.frame.core.constant.UniqueIdType;
import com.bonelf.frame.web.security.BaseApiAuthenticationToken;
import com.bonelf.frame.web.security.domain.AuthUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * token 信息填充
 * @author bonelf
 * @date 2021/5/24 13:47
 */
public class JwtWithUserInfoAccessTokenConverter extends DefaultAccessTokenConverter {

	public JwtWithUserInfoAccessTokenConverter() {
		super.setUserTokenConverter(new JwtUserAuthenticationConverter());
	}

	/**
	 * 原默认只放了username
	 * @author bonelf
	 */
	private class JwtUserAuthenticationConverter implements UserAuthenticationConverter {
		//返回的map最终会被编码成json串作为jwt claim
		@Override
		public Map<String, ?> convertUserAuthentication(Authentication authentication) {//oauth2server在check token时用到?把用户信息转换成key-value值返回
			Map<String, String> response = new HashMap<>();
			//目前对user对象序列化按JwtClaimView序列化,若不控制则可以直接往结果中设置user对象即可
			if (authentication instanceof BaseApiAuthenticationToken) {
				response.put("user_id", String.valueOf(((BaseApiAuthenticationToken)authentication).getUserId()));
			}
			if (authentication.getPrincipal() instanceof AuthUser) {
				AuthUser user = (AuthUser)authentication.getPrincipal();
				if (!response.containsKey("user_id")) {
					response.put("user_id", String.valueOf(user.getUserId()));
				}
				response.put("uniqueId", user.getUsername());
				response.put("id_type", user.getIdType().name());
			}
			return response;
		}

		@Override
		public Authentication extractAuthentication(Map<String, ?> map) {
			//不从数据库加载，直接从jwt中恢复用户信息;
			if (map.containsKey("user_id")) {
				AuthUser principal = new AuthUser((String)map.get("username"), UniqueIdType.valueOf((String)map.get("id_type")), "N/A");
				principal.setUserId((Long)map.get("user_id"));
				return new UsernamePasswordAuthenticationToken(principal, principal, principal.getAuthorities());
			}
			return null;
		}
	}
}