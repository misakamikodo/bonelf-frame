package com.bonelf.auth.service.impl;

import com.bonelf.auth.service.SupportService;
import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
import com.bonelf.frame.core.domain.Result;
import org.springframework.stereotype.Service;

/**
 * TODO 工具服务
 * @author ccy
 * @date 2021/7/27 13:41
 */
@Service
public class SupportServiceImpl implements SupportService {

	@Override
	public String getVerifyMail(String mail, VerifyCodeTypeEnum code) {
		return "1234";
	}

	@Override
	public String getVerifyPhone(String phone, VerifyCodeTypeEnum code) {
		return "1234";
	}
}
