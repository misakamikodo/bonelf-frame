package com.bonelf.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bonelf.auth.constant.UserStatusEnum;
import com.bonelf.auth.constant.exception.UserExceptionEnum;
import com.bonelf.auth.web.domain.entity.DbUser;
import com.bonelf.auth.web.mapper.DbUserMapper;
import com.bonelf.frame.base.property.BonelfProperties;
import com.bonelf.frame.core.auth.domain.RegisterUserAO;
import com.bonelf.frame.core.auth.domain.User;
import com.bonelf.frame.core.auth.service.AuthRoleService;
import com.bonelf.frame.core.auth.service.AuthUserService;
import com.bonelf.frame.core.constant.BonelfConstant;
import com.bonelf.frame.core.constant.UsernameType;
import com.bonelf.frame.core.constant.enums.YesOrNotEnum;
import com.bonelf.frame.core.exception.BonelfException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthUserServiceImpl implements AuthUserService {
	@Autowired
	private DbUserMapper dbUserMapper;
	@Autowired
	private AuthRoleService authRoleService;
	@Autowired
	private BonelfProperties bonelfProperties;

	/**
	 * 根据用户唯一标识获取用户信息
	 * @param uniqueId uniqueId
	 * @return
	 */
	@Override
	//@Cacheable(value = "#id", condition = "#result.getSuccess()")
	public User getByUniqueId(String uniqueId, UsernameType idType) {
		return getByUniqueId(uniqueId, new UsernameType[]{idType});
	}

	/**
	 * 根据用户唯一标识获取用户信息,没有的话则注册
	 * @param uniqueId
	 * @param idType
	 * @param userMsg
	 * @return
	 */
	@Override
	public User getByUniqueIdOrElseRegister(String uniqueId, UsernameType idType, RegisterUserAO userMsg) {
		return getByUniqueId(uniqueId, new UsernameType[]{idType});
	}


	/**
	 * 封装用户
	 * @param dbUser
	 * @return
	 */
	private User getUserFromDbUser(DbUser dbUser) {
		User userResult = new User();
		BeanUtil.copyProperties(dbUser, userResult);
		userResult.setEnabled(YesOrNotEnum.N.getCode().equals(dbUser.getStatus()));
		userResult.setAccountNonExpired(true);
		userResult.setCredentialsNonExpired(true);
		userResult.setAccountNonLocked(YesOrNotEnum.N.getCode().equals(dbUser.getStatus()));
		userResult.setRoles(authRoleService.queryUserRolesByUserId(dbUser.getUserId()));
		return userResult;
	}

	@Override
	public User getByUniqueId(String uniqueId, UsernameType[] idTypes) {
		DbUser dbUser;
		if (idTypes == null || idTypes.length == 0) {
			dbUser = dbUserMapper.selectOne(Wrappers.<DbUser>lambdaQuery()
					.eq(DbUser::getUserId, uniqueId).or()
					.eq(DbUser::getUsername, uniqueId).or()
					.eq(DbUser::getPhone, uniqueId).or()
					.eq(DbUser::getMail, uniqueId).or()
					.eq(DbUser::getOpenId, uniqueId).orderByDesc(DbUser::getUpdateTime).last("limit 1"));
		} else {
			LambdaQueryWrapper<DbUser> lqw = Wrappers.<DbUser>lambdaQuery()
					.orderByDesc(DbUser::getUpdateTime).last("limit 1");
			for (int i = 0; i < idTypes.length - 1; i++) {
				UsernameType idType = idTypes[i];
				switch (idType) {
					case id:
						lqw = lqw.eq(DbUser::getUserId, uniqueId).or();
						break;
					case username:
						lqw = lqw.eq(DbUser::getUsername, uniqueId).or();
						break;
					case mail:
						lqw = lqw.eq(DbUser::getMail, uniqueId).or();
						break;
					case phone:
						lqw = lqw.eq(DbUser::getPhone, uniqueId).or();
						break;
					case openId:
						lqw = lqw.eq(DbUser::getOpenId, uniqueId).or();
						break;
					case unionId:
						lqw = lqw.eq(DbUser::getUnionId, uniqueId).or();
						break;
					default:
						throw new BonelfException(String.format("unknown type %s of username", idType.toString()));
				}
			}
			switch (idTypes[idTypes.length - 1]) {
				case id:
					lqw.eq(DbUser::getUserId, uniqueId);
					break;
				case username:
					lqw.eq(DbUser::getUsername, uniqueId);
					break;
				case mail:
					lqw.eq(DbUser::getMail, uniqueId);
					break;
				case phone:
					lqw.eq(DbUser::getPhone, uniqueId);
					break;
				case openId:
					lqw.eq(DbUser::getOpenId, uniqueId);
					break;
				case unionId:
					lqw.eq(DbUser::getUnionId, uniqueId);
					break;
				default:
					throw new BonelfException(String.format("unknown type %s of username", uniqueId.toString()));
			}
			dbUser = dbUserMapper.selectOne(lqw);
		}
		return getUserFromDbUser(dbUser);
	}

	/**
	 * 注册用户合理性检查
	 * @param past
	 * @return
	 */
	private void registerUserCheck(DbUser past) {
		if (past != null) {
			if (UserStatusEnum.FREEZE.getCode().equals(past.getStatus())) {
				throw new BonelfException(UserExceptionEnum.FREEZE_USER);
			}
			throw new BonelfException(UserExceptionEnum.ALREADY_REGISTER);
		}
	}

	@Override
	public User registerByPhone(String phone) {
		DbUser past = dbUserMapper.selectOne(Wrappers.<DbUser>lambdaQuery().eq(DbUser::getPhone, phone));
		registerUserCheck(past);
		DbUser user = new DbUser();
		user.setAvatar(bonelfProperties.getBaseUrl() + BonelfConstant.DEFAULT_AVATAR_PATH);
		user.setPhone(phone);
		user.setLastLoginTime(LocalDateTime.now());
		dbUserMapper.insert(user);
		dbUserMapper.update(new DbUser(),
				Wrappers.<DbUser>lambdaUpdate().set(DbUser::getNickname, "手机用户").eq(DbUser::getUserId, user.getUserId()));
		return getUserFromDbUser(user);
	}

	@Override
	public User registerByMail(String mail) {
		DbUser past = dbUserMapper.selectOne(Wrappers.<DbUser>lambdaQuery().eq(DbUser::getMail, mail));
		registerUserCheck(past);
		DbUser user = new DbUser();
		user.setAvatar(bonelfProperties.getBaseUrl() + BonelfConstant.DEFAULT_AVATAR_PATH);
		user.setPhone(mail);
		user.setLastLoginTime(LocalDateTime.now());
		dbUserMapper.insert(user);
		dbUserMapper.update(new DbUser(),
				Wrappers.<DbUser>lambdaUpdate().set(DbUser::getNickname, "邮箱用户").eq(DbUser::getUserId, user.getUserId()));
		return getUserFromDbUser(user);
	}

	@Override
	public User registerByOpenId(RegisterUserAO registerUser) {
		DbUser past = dbUserMapper.selectOne(Wrappers.<DbUser>lambdaQuery()
				.eq(DbUser::getOpenId, registerUser.getOpenId()).or()
				.eq(DbUser::getUnionId, registerUser.getUnionId()).last("limit 1"));
		registerUserCheck(past);
		DbUser user = BeanUtil.copyProperties(registerUser, DbUser.class);
		if (user.getAvatar() == null) {
			user.setAvatar(bonelfProperties.getBaseUrl() + BonelfConstant.DEFAULT_AVATAR_PATH);
		}
		user.setLastLoginTime(LocalDateTime.now());
		dbUserMapper.insert(user);
		if (user.getNickname() == null) {
			dbUserMapper.update(new DbUser(),
					Wrappers.<DbUser>lambdaUpdate().set(DbUser::getNickname, "微信").eq(DbUser::getUserId, user.getUserId()));
		}
		return null;
	}
}
