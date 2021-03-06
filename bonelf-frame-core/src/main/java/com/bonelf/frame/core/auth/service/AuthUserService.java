package com.bonelf.frame.core.auth.service;

import com.bonelf.frame.core.auth.domain.RegisterUserAO;
import com.bonelf.frame.core.auth.domain.User;
import com.bonelf.frame.core.constant.UsernameType;

/**
 * <p>
 * 签权服务
 * </p>
 * @author bonelf
 * @since 2020/11/17 15:37
 */
public interface AuthUserService {

	User getById(String userId);

    /**
     * 根据用户唯一标识获取用户信息
     * @param uniqueId
     * @param idType
     * @return
     */
	User getByUniqueId(String uniqueId, UsernameType idType);

	User getByUniqueId(String uniqueId, UsernameType[] idType);

	/**
	 * 注册
	 * @param phone
	 * @return
	 */
	User registerByPhone(String phone);
	/**
	 * 注册
	 * @param mail
	 * @return
	 */
	User registerByMail(String mail);

	/**
	 * 微信注册
	 * @param registerUser
	 * @return
	 */
	User registerByOpenId(RegisterUserAO registerUser);

	/**
	 * 根据用户唯一标识获取用户信息,没有的话则注册
	 * 不可使用，因为还没进行验证码之类的校验
	 * @param uniqueId
	 * @return
	 */
	@Deprecated
	User getByUniqueIdOrElseRegister(String uniqueId, UsernameType idType, RegisterUserAO userMsg);
}
