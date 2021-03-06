/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.support.constant;

import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
import com.bonelf.frame.core.constant.BonelfConstant;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public interface CacheConstant {

	/**
	 * socket session hash 存储在线状态
	 */
	String WEB_SOCKET_SESSION_HASH = BonelfConstant.PROJECT_NAME + ":websocket:session";

	/*
	 * session 有效时间 如果不使用hash表存储session使用
	 */
	//long SESSION_TIME = -1;

	/**
	 * 验证码过期时间
	 */
	long VERIFY_CODE_EXPIRED_SECOND = 5 * 60L;

	/**
	 * 验证码 businessType {phone、random uuid}
	 * @see VerifyCodeTypeEnum
	 */
	String VERIFY_CODE = BonelfConstant.PROJECT_NAME + ":%s:%s";
	/**
	 * 时效二维码数据缓存 uniqueId
	 */
	String QR_CODE_PREFIX = BonelfConstant.PROJECT_NAME + ":qrcode:%s";
	/**
	 * 时效二维码过期时间 s
	 */
	long QR_CODE_EXPIRE_TIME = 30 * 60L;
	long QR_CODE_MAX_EXPIRE_TIME = 30 * 24 * 60 * 60L;
	/**
	 * 用户缓存消息
	 */
	String SOCKET_MSG = BonelfConstant.PROJECT_NAME + ":socketmsg:%s";
	/**
	 * 用户缓存消息:15天
	 */
	long SOCKET_MSG_TIME = 15;

	/**
	 * 文件秒传
	 */
	String FILE_MUILT_UPLOAD_MD5 = BonelfConstant.PROJECT_NAME + ":md5:%s";
	/**
	 * 文件秒传过期时间
	 */
	Duration FILE_MUILT_UPLOAD_EXPIRE = Duration.of(1, ChronoUnit.YEARS);
}
