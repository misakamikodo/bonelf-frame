package com.bonelf.frame.gateway.filter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.bonelf.frame.base.property.BonelfProperties;
import com.bonelf.frame.cloud.security.constant.AuthFeignConstant;
import com.bonelf.frame.core.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

/**
 * 接口处理请求信息
 * 鉴权卸载JwtFilter中
 * 如果不使用Shiro可以写在这里就好
 * @author bonelf
 * @date 2020-10-12 11:53:08
 */
@Slf4j
@Component
public class GlobalAccessFilter implements GlobalFilter, Ordered {
	@Value("${server.servlet.context-path:}")
	private String ctxPath;

	//@Value("#{'${bonelf.no-auth-url:}'.split(',')}")
	private final List<String> permitPath;

	public GlobalAccessFilter(BonelfProperties bonelfProperty) {
		List<String> permitPath = new ArrayList<>();
		for (String s : bonelfProperty.getPermitPath()) {
			permitPath.addAll(CollectionUtil.toList(s.split(StrUtil.COMMA)));
		}
		this.permitPath = permitPath;
	}

	// public static void main(String[] args) {
	// 	long stripPrefix = StrUtil.count("/bonelf", "/") + 1L;
	// 	System.out.println(
	// 			"/bonelf" + "/" + Arrays.stream(StringUtils.tokenizeToStringArray("/bonelf/test/noAuth/xxx", "/")).skip(stripPrefix).collect(Collectors.joining("/"))
	// 	);
	// }

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		//有可能有空值bug，所以去空
		//CollectionUtil.removeNull(noAuthUrl);
		ServerHttpRequest request = exchange.getRequest();
		String authentication = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		String method = request.getMethodValue();
		String url = request.getPath().value();
		log.debug("url:{},method:{},headers:{}", url, method, request.getHeaders());

		String rawPath = exchange.getRequest().getURI().getRawPath();
		//这段代码要求所有子服务不配置context-path 若配置和gateway一样的context-path 删除StringUtil.appearTimes(ctxPath, "/")
		long stripPrefix = StrUtil.count(ctxPath, "/") + 1L;
		String newPath = ctxPath + "/" + Arrays.stream(StringUtils.tokenizeToStringArray(rawPath, "/"))
				.skip(stripPrefix).collect(Collectors.joining("/"));
		//不需要网关签权的url XXX 没法引入 AntPathRequestMatcher(pattern, method)
		if (CollectionUtil.isEmpty(permitPath) || !CollectionUtil.contains(permitPath, newPath.replaceFirst(ctxPath, ""))) {
			// 如果请求未携带token信息, 直接跳出
			// if (StringUtils.isEmpty(authentication) || !authentication.startsWith(OAuth2Constant.TOKEN_PREFIX)) {
			// 	log.debug("url:{},method:{},headers:{}, 请求未携带token信息", url, method, request.getHeaders());
			// 	return unauthorized(exchange);
			// }
			// 过了Auth服务直接完成权限校验，其他服务不必再鉴权 FIXME 签权Sign
			// if (!authService.hasPermission(authentication, url, method)) {
			//	return unauthorized(exchange);
			// }
		}

		// 1. 重写StripPrefix(获取真实的URL)
		addOriginalRequestUrl(exchange, exchange.getRequest().getURI());
		String originAuthHeader = exchange.getRequest().getHeaders().getFirst(AuthFeignConstant.AUTH_HEADER);
		ServerHttpRequest newRequest = exchange.getRequest().mutate()
				.path(newPath)
				//将现在的request，添加当前身份 (标识，可以存放redis加强严谨性) XXX 添加调用方名称
				.header(AuthFeignConstant.AUTH_HEADER, originAuthHeader == null ? "-" : originAuthHeader)
				.build();
		exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, newRequest.getURI());
		//.header("claims信息", authService.getJwt(authentication).getClaims());

		//原来newPath不生效的代码，我认为网上作者写错了的
		//ServerHttpRequest mutableReq = exchange.getRequest().mutate().header(AuthFeignConstant.AUTH_HEADER, "").build();
		//ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();

		ServerWebExchange mutableExchange = exchange.mutate().request(newRequest).build();
		return chain.filter(mutableExchange);
	}

	@Override
	public int getOrder() {
		return -200;
	}

	/**
	 * 网关拒绝，返回401
	 *
	 * @param
	 */
	private Mono<Void> unauthorized(ServerWebExchange serverWebExchange) {
		Result<?> result = Result.error("401", HttpStatus.UNAUTHORIZED.getReasonPhrase());
		serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		DataBuffer buffer = serverWebExchange.getResponse()
				.bufferFactory().wrap(JSONUtil.toJsonStr(result).getBytes());
		return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
	}

}
