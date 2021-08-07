package com.bonelf.support.web.service;


import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;

public interface MailService {
	/**
	 * 发送验证码
	 * @param username
	 * @param bizType
	 * @return
	 */
	String sendVerify(String username, VerifyCodeTypeEnum bizType);
}
