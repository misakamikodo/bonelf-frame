package com.bonelf.support.web.service;


import com.bonelf.support.constant.VerifyCodeTypeEnum;

public interface SmsService{
	/**
	 * 发送验证码
	 * @param username
	 * @param bizType
	 * @return
	 */
	String sendVerify(String username, VerifyCodeTypeEnum bizType);
}
