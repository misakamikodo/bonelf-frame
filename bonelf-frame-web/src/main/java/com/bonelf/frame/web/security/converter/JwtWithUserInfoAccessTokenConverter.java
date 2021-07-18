package com.bonelf.frame.web.security.converter;

import com.bonelf.frame.core.constant.UsernameType;
import com.bonelf.frame.web.security.BaseApiAuthenticationToken;
import com.bonelf.frame.web.security.domain.AuthUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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

	public JwtWithUserInfoAccessTokenConverter(UserDetailsService userDetailsService) {
		super.setUserTokenConverter(new JwtUserAuthenticationConverter(userDetailsService));
	}

	/**
	 * 原默认只放了username
	 * @author bonelf
	 */
	private class JwtUserAuthenticationConverter implements UserAuthenticationConverter {
		private UserDetailsService userDetailsService;

		public JwtUserAuthenticationConverter(UserDetailsService userDetailsService) {
			this.userDetailsService = userDetailsService;
		}

		/**
		 * Optional {@link UserDetailsService} to use when extracting an {@link Authentication} from the incoming map.
		 * @param userDetailsService the userDetailsService to set
		 */
		public void setUserDetailsService(UserDetailsService userDetailsService) {
			this.userDetailsService = userDetailsService;
		}

		//返回的map最终会被编码成json串作为jwt claim
		@Override
		public Map<String, ?> convertUserAuthentication(Authentication authentication) {//oauth2server在check token时用到?把用户信息转换成key-value值返回
			Map<String, Object> response = new HashMap<>();
			//目前对user对象序列化按JwtClaimView序列化,若不控制则可以直接往结果中设置user对象即可
			if (authentication instanceof BaseApiAuthenticationToken) {
				response.put("user_id", String.valueOf(((BaseApiAuthenticationToken)authentication).getUserId()));
			}
			if (authentication.getPrincipal() instanceof AuthUser) {
				AuthUser user = (AuthUser)authentication.getPrincipal();
				if (!response.containsKey("user_id")) {
					response.put("user_id", String.valueOf(user.getUserId()));
				}
				if (userDetailsService == null) {
					response.put("uniqueId", user.getUsername());
					response.put("id_type", user.getUsernameType().name());
				}
				// 改成userDetailService获取
				// response.put("authorities", user.getAuthorities());
			}
			return response;
		}

		@Override
		public Authentication extractAuthentication(Map<String, ?> map) {
			//不从数据库加载，直接从jwt中恢复用户信息;
			if (map.containsKey("user_id")) {
				// List<Map<String, String>> authorityList = (List<Map<String, String>>)map.get("authorities");
				// List<SimpleGrantedAuthority> authorities = authorityList.stream().map(item->{
				// 	SimpleGrantedAuthority authority = new SimpleGrantedAuthority(item.get("authority"));
				// 	return authority;
				// }).collect(Collectors.toList());
				// List<SimpleGrantedAuthority> authorities = new ArrayList<>();
				UserDetails principal;
				if (userDetailsService != null) {
					principal = userDetailsService.loadUserByUsername(String.valueOf(map.get("user_id")));
				} else {
					// 这样没有权限校验，上面注释的是把权限写到token，这样token内容太庞大，不太好
					principal = new AuthUser((String)map.get("username"),
							UsernameType.valueOf((String)map.get("id_type")),
							"N/A");
				}
				if (principal instanceof AuthUser) {
					AuthUser authUser = (AuthUser)principal;
					authUser.setUserId((Long)map.get("user_id"));
					return new UsernamePasswordAuthenticationToken(authUser, authUser, authUser.getAuthorities());
				}
				// default
				return new UsernamePasswordAuthenticationToken(principal, principal, principal.getAuthorities());
			}
			return null;
		}
	}
}