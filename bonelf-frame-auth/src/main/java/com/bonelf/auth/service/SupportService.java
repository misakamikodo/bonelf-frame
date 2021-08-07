package com.bonelf.auth.service;

import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
import com.bonelf.frame.core.domain.Result;

/**
 * 工具服务接口
 * @author ccy
 * @date 2021/7/27 13:41
 */
public interface SupportService {
	/**
	 * 邮箱验证码
	 * @param mail
	 * @param code 业务类型 {@link VerifyCodeTypeEnum}
	 * @return
	 */
	String getVerifyMail(String mail, VerifyCodeTypeEnum code);

	/**
	 * 手机验证码
	 * @param phone
	 * @param code
	 * @return
	 */
	String getVerifyPhone(String phone, VerifyCodeTypeEnum code);
}
