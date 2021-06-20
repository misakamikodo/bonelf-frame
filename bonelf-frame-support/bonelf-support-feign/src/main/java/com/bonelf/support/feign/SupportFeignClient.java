package com.bonelf.support.feign;

import com.bonelf.frame.cloud.feign.FeignConfig;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.core.websocket.SocketRespMessage;
import com.bonelf.support.feign.domain.request.DictValueRequest;
import com.bonelf.support.feign.domain.response.DictTextResponse;
import com.bonelf.support.feign.factory.SupportFeignFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 服务提供 服务 feign
 * @author bonelf
 * @since 2020/11/17 15:37
 */
@FeignClient(contextId = "supportFeignClient", value = "support",
		configuration = FeignConfig.class, fallbackFactory = SupportFeignFallbackFactory.class)
public interface SupportFeignClient {

	@PostMapping("/bonelf/websocket/v1/sendMessage")
	Result<String> sendMessage(@RequestBody SocketRespMessage message);

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
	@PostMapping(value = "/bonelf/sms/v1/sendVerify")
	Result<String> sendVerify(@RequestParam("phone") String phone, @RequestParam("businessType") String businessType);

	/**
	 * 获取验证码
	 * @param phone 手机号
	 * @param businessType {@link com.bonelf.support.feign.domain.constant.VerifyCodeTypeEnum 短信类型}
	 * @return 验证码
	 */
	@GetMapping(value = "/bonelf/sms/v1/getVerify")
	Result<String> getVerifyPhone(@RequestParam("phone") String phone, @RequestParam("businessType") String businessType);

	@GetMapping(value = "/bonelf/sms/v1/getVerify")
	Result<String> getVerifyMail(@RequestParam("mail") String mail, @RequestParam("businessType") String businessType);

	@GetMapping(value = "/bonelf/sys/dbdict/v1/getTextByValue")
	Result<String> selectDictTextByItemValue(@RequestParam("dictId") String dictId, @RequestParam("itemValue") String itemValue);

	@RequestMapping(value = "/bonelf/sys/dbdict/v1/getTextByValueBatch")
	Result<Set<DictTextResponse>> selectDictTextByItemValueBatch(@RequestBody Set<DictValueRequest> query);
}
