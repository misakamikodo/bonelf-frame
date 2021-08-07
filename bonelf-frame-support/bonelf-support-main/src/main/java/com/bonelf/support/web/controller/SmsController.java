package com.bonelf.support.web.controller;

import com.bonelf.cicada.util.EnumUtil;
import com.bonelf.frame.base.util.redis.RedisUtil;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
import com.bonelf.support.constant.exception.SupportExceptionEnum;
import com.bonelf.support.web.domain.dto.VerifyCodeDTO;
import com.bonelf.support.web.service.MailService;
import com.bonelf.support.web.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务在线管理
 * @author bonelf
 * @date 2019-01-02
 */
@RestController
@RequestMapping("/sms")
@Slf4j
@Api(tags = "短信服务接口")
public class SmsController {
	@Autowired
	private SmsService smsService;
	@Autowired
	private MailService mailService;
	@Autowired
	private RedisUtil redisUtil;


	@ApiOperation("验证码")
	@PostMapping(value = "/v1/sendVerify")
	public Result<String> sendVerify(@Validated @RequestBody VerifyCodeDTO accountLoginDto) {
		// FIXME: 2020/11/2 投入使用后删除此返回值 移动到Support服务
		if (accountLoginDto.getMail() != null) {
			String code = mailService.sendVerify(accountLoginDto.getMail(),
					EnumUtil.getByCode(accountLoginDto.getBusinessType(),
							VerifyCodeTypeEnum.class));
			return Result.ok(code);
		} else if (accountLoginDto.getPhone() != null) {
			String code = smsService.sendVerify(accountLoginDto.getPhone(),
					EnumUtil.getByCode(accountLoginDto.getBusinessType(),
							VerifyCodeTypeEnum.class));
			return Result.ok(code);
		} else {
			return Result.error(CommonBizExceptionEnum.REQUEST_INVALIDATE);
		}
	}


	@ApiOperation("获取验证码")
	@GetMapping(value = "/v1/getVerify")
	public Result<String> getVerify(VerifyCodeDTO accountLoginDto) {
		VerifyCodeTypeEnum codeType = EnumUtil.getByCode(accountLoginDto.getBusinessType(), VerifyCodeTypeEnum.class);
		String target;
		if (accountLoginDto.getMail() != null) {
			target = accountLoginDto.getMail();
		} else if (accountLoginDto.getPhone() != null) {
			target = accountLoginDto.getPhone();
		} else {
			return Result.error(CommonBizExceptionEnum.REQUEST_INVALIDATE);
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
			return Result.error(SupportExceptionEnum.VERIFY_CODE_EXPIRE);
		}
		return Result.ok(code);
	}
}
