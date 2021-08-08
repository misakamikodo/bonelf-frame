package com.bonelf.support.web.service;


import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
import com.bonelf.support.web.domain.dto.VerifyCodeDTO;

public interface SmsService{
	/**
	 * 发送验证码
	 * @param username
	 * @param bizType
	 * @return
	 */
	String sendVerify(String username, VerifyCodeTypeEnum bizType);

	String getVertify(VerifyCodeDTO accountLoginDto);
}
