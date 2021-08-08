package com.bonelf.support.web.service.impl;


import cn.hutool.core.util.RandomUtil;
import com.bonelf.cicada.util.EnumUtil;
import com.bonelf.frame.base.util.SmsUtil;
import com.bonelf.frame.base.util.redis.RedisUtil;
import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.support.constant.exception.SupportExceptionEnum;
import com.bonelf.support.web.domain.dto.VerifyCodeDTO;
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

	@Override
	public String getVertify(VerifyCodeDTO accountLoginDto) {
		VerifyCodeTypeEnum codeType = EnumUtil.getByCode(accountLoginDto.getBusinessType(), VerifyCodeTypeEnum.class);
		String target;
		if (accountLoginDto.getMail() != null) {
			target = accountLoginDto.getMail();
		} else if (accountLoginDto.getPhone() != null) {
			target = accountLoginDto.getPhone();
		} else {
			throw new BonelfException(CommonBizExceptionEnum.REQUEST_INVALIDATE);
		}
		String key = String.format(CacheConstant.VERIFY_CODE, accountLoginDto.getBusinessType(), target);
		String code = (String)redisUtil.get(key);
		switch (codeType) {
			case LOGIN:
				if (code != null) {
					redisUtil.del(key);
				}
				break;
			default:
		}
		if (code == null) {
			throw new BonelfException(SupportExceptionEnum.VERIFY_CODE_EXPIRE);
		}
		return code;
	}
}
