package com.bonelf.frame.web.aop;

import com.bonelf.frame.base.util.redis.RedisLock;
import com.bonelf.frame.core.constant.CommonCacheConstant;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.frame.web.aop.constraints.NoRepeatSubmit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * 防止重复提交切面
 **/
@Aspect
@Component
@Slf4j
public class RepeatSubmitAspect {

	@Autowired
	private RedisLock redisLock;

	@Autowired
	private HttpServletRequest request;

	@Pointcut("@annotation(noRepeatSubmit)")
	public void pointCut(NoRepeatSubmit noRepeatSubmit) {
	}

	@Around(value = "pointCut(noRepeatSubmit)", argNames = "point,noRepeatSubmit")
	public Object around(ProceedingJoinPoint point, NoRepeatSubmit noRepeatSubmit) throws Throwable {
		// 锁定时间  // noRepeatSubmit.lockTime() 获取的是秒
		long lockSeconds = noRepeatSubmit.lockTime();
		Assert.notNull(request, "request can not null");

		// 此处可以用token或者JSessionId
		String jSessionId = request.getSession().getId();
		String path = request.getServletPath();
		String key = getKey(jSessionId, path);
		String clientId = getClientId();
		boolean isSuccess = redisLock.tryLock(key, clientId, lockSeconds);
		if (isSuccess) {
			log.info("tryLock success, key = [{}], clientId = [{}] lockSeconds=[{}]", key, clientId, lockSeconds);
			try {
				// 获取锁成功, 执行进程
				return point.proceed();
			} catch (Exception e) {
				// 发生异常解锁
				redisLock.unlock(key, clientId);
				log.info("releaseLock success, key = [{}], clientId = [{}]", key, clientId);
				throw e;
			}
		} else {
			// 获取锁失败，认为是重复提交的请求
			log.info("tryLock fail, key = [{}]", key);
			// 400 请误重复提交
			throw new BonelfException(CommonBizExceptionEnum.NO_REPEAT_SUBMIT, (redisLock.getExpiredTime(getKey(jSessionId, path)) + 1));
		}
	}

	/**
	 * redis键值
	 * @param token sessionId
	 * @param path servletpath
	 * @return redis键值
	 */
	private String getKey(String token, String path) {
		return String.format(CommonCacheConstant.NO_REPEAT_SUBMIT, token, path);
	}

	private String getClientId() {
		return UUID.randomUUID().toString();
	}

}
