package com.bonelf.support.feign;

import com.bonelf.frame.cloud.feign.FeignConfig;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.support.feign.factory.SupportFeignFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 服务提供 服务 feign
 */
@FeignClient(contextId = "supportFeignClient", value = "support",
		configuration = FeignConfig.class, fallbackFactory = SupportFeignFallbackFactory.class)
public interface SupportFeignClient {

	@PostMapping("/websocket/v1/sendMessage")
	Result<String> sendMessage(@RequestBody SocketRespMessage message);

	@GetMapping("/sys/dbdict/v1/getByCode")
	Result<String> queryDictTextByKey(@RequestParam("code") String code, @RequestParam("value") String value);

	/**
	 * 获取用户服务缓存中的验证码，不是生成验证码
	 * 可以分离成短信服务/三方服务
	 * 现在直接从getUserByUniqueId获取到验证码 存于password字段中
	 * passwordEncoder.encode("980826")
	 * 由userService调用
	 * @param phone 手机号
	 * @param businessType 短信类型 {@link com.bonelf.support.feign.domain.constant.VerifyCodeTypeEnum}
	 * @return 验证码
	 */
	@PostMapping(value = "/sms/v1/sendVerify")
	Result<String> sendVerify(@RequestParam("phone") String phone, @RequestParam("businessType") String businessType);

	/**
	 * 获取验证码
	 * @param phone 手机号
	 * @param businessType 短信类型 {@link com.bonelf.support.feign.domain.constant.VerifyCodeTypeEnum}
	 * @return 验证码
	 */
	@GetMapping(value = "/sms/v1/getVerify")
	Result<String> getVerify(@RequestParam("phone") String phone, @RequestParam("businessType") String businessType);
}
