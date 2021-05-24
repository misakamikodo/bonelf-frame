package com.bonelf.frame.cloud.feign;

import cn.hutool.core.util.StrUtil;
import com.bonelf.frame.cloud.security.constant.AuthFeignConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


/**
 * 给feign请求添加请求头
 * @author bonelf
 * @date 2021/4/20 17:53
 */
@Slf4j
@Configuration
public class FeignConfig implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate requestTemplate) {
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if (attributes != null) {
			HttpServletRequest request = attributes.getRequest();
			String header = request.getHeader(AuthFeignConstant.AUTH_HEADER);

			// TODO 看是否可以添加从什么服务来的
			log.debug(AuthFeignConstant.AUTH_HEADER + ":" + header);
			requestTemplate.header(AuthFeignConstant.AUTH_HEADER, AuthFeignConstant.FEIGN_REQ_FLAG_PREFIX + (StrUtil.isEmpty(header) ? "" : " ") + header);
		}
	}
}
