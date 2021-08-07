package com.bonelf.support.web.service.impl;


import cn.hutool.core.util.RandomUtil;
import com.bonelf.frame.base.util.SmsUtil;
import com.bonelf.frame.base.util.redis.RedisUtil;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
import com.bonelf.support.web.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.bonelf.frame.base.util.SmsUtil.SEND_OK;

@Service
public class SmsServiceImpl implements SmsService {

	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private SmsUtil smsUtil;

	@Override
	public String sendVerify(String phone, VerifyCodeTypeEnum bizType) {
		if (redisUtil.get(String.format(CacheConstant.VERIFY_CODE, bizType.getCode(), phone)) != null) {
			throw new BonelfException(CommonBizExceptionEnum.NO_REPEAT_SUBMIT, redisUtil.getExpire(String.format(CacheConstant.VERIFY_CODE, bizType.getCode(), phone)));
		}
		String code = RandomUtil.randomNumbers(6);
		Map<String, Object> result = smsUtil.sendVerify(phone, code);
		redisUtil.set(String.format(CacheConstant.VERIFY_CODE, bizType.getCode(),  phone), code, CacheConstant.VERIFY_CODE_EXPIRED_SECOND);
		if (!SEND_OK.equals(result.get("Message"))) {
			throw BonelfException.builder("短信发送失败").build();
		}
		return code;
	}
}
