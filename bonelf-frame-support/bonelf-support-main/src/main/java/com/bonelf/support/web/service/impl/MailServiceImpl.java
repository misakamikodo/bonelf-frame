package com.bonelf.support.web.service.impl;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.mail.MailAccount;
import com.bonelf.frame.base.property.BonelfMailProperties;
import com.bonelf.frame.base.property.BonelfProperties;
import com.bonelf.frame.base.util.redis.RedisUtil;
import com.bonelf.frame.core.constant.BonelfConstant;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
import com.bonelf.support.web.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private BonelfProperties bonelfProperties;
	@Autowired
	private BonelfMailProperties bonelfMailProperties;

	@Override
	public String sendVerify(String mail, VerifyCodeTypeEnum bizType) {
		if (redisUtil.get(String.format(CacheConstant.VERIFY_CODE, bizType.getCode(), mail)) != null) {
			throw new BonelfException(CommonBizExceptionEnum.NO_REPEAT_SUBMIT, redisUtil.getExpire(String.format(CacheConstant.VERIFY_CODE, bizType.getCode(), mail)));
		}
		String code = RandomUtil.randomNumbers(6);
		// mailUtil.sendVerifyMail(mail, code);
		MailAccount account = new MailAccount();
		account.setHost(bonelfMailProperties.getSmtp());
		account.setAuth(true);
		account.setFrom(bonelfMailProperties.getUsername());
		account.setUser(bonelfMailProperties.getUsername());
		account.setPass(bonelfMailProperties.getPassword());
		cn.hutool.extra.mail.MailUtil.send(account, mail, bonelfProperties.getAppName(),
				BonelfConstant.VERIFY_HTML.replace("{CODE}", code).replace("{APPNAME}", bonelfProperties.getAppName()), true);
		redisUtil.set(String.format(CacheConstant.VERIFY_CODE, bizType.getCode(),  mail), code, CacheConstant.VERIFY_CODE_EXPIRED_SECOND);
		return code;
	}
}
