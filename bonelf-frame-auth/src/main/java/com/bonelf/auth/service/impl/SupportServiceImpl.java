package com.bonelf.auth.service.impl;

import com.bonelf.auth.service.SupportService;
import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
import com.bonelf.support.config.SupportConfig;
import com.bonelf.support.web.domain.dto.VerifyCodeDTO;
import com.bonelf.support.web.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * 工具服务
 * @author bonelf
 * @date 2021/7/27 13:41
 */
@ConditionalOnClass(SupportConfig.class)
@Service("defaultSupportService")
public class SupportServiceImpl implements SupportService {
	@Autowired(required = false)
	private SmsService smsService;

	@Override
	public String getVerifyMail(String mail, VerifyCodeTypeEnum code) {
		VerifyCodeDTO dto = new VerifyCodeDTO();
		dto.setMail(mail);
		dto.setBusinessType(code.getCode());
		return smsService.getVertify(dto);
	}

	@Override
	public String getVerifyPhone(String phone, VerifyCodeTypeEnum code) {
		VerifyCodeDTO dto = new VerifyCodeDTO();
		dto.setMail(phone);
		dto.setBusinessType(code.getCode());
		return smsService.getVertify(dto);
	}
}
