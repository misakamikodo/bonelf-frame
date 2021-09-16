package com.bonelf.frame.web.core.advice;

import cn.hutool.core.util.StrUtil;
import com.bonelf.frame.core.constant.BizConstants;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.frame.core.exception.enums.DataAccessExceptionEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

/**
 * <p>
 * 异常处理
 * </p>
 * @author Chenyuan
 * @since 2021/2/2 11:51
 */
@Slf4j
@RestControllerAdvice
@Order(-1)
public class GlobalExceptionHandler {

	/**
	 * BonelfException API异常
	 * @param e
	 * @return
	 */
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(value = BonelfException.class)
	public Result<?> errorHandler(BonelfException e) {
		//便于调试
		e.printStackTrace();
		return Result.error(e.getCode(), e.getErrorMessage());
	}

	/**
	 * 处理Get请求中 使用@Valid 验证路径中请求实体校验失败后抛出的异常BindException
	 * 处理@RequestParam上validate失败后抛出的异常是ConstraintViolationException
	 * 处理请求参数格式错误 @RequestBody上validate失败后抛出的异常是MethodArgumentNotValidException
	 * 拦截自定义参数验证失败异常
	 */

	@ExceptionHandler({BindException.class,
			ConstraintViolationException.class,
			MethodArgumentNotValidException.class,
			MissingServletRequestParameterException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<?> handleApiConstraintViolationException(Exception e, HttpServletRequest request) {
		String message = null;
		if (e instanceof BindException) {
			message = ((BindException)e).getBindingResult().getAllErrors().stream()
					.map(DefaultMessageSourceResolvable::getDefaultMessage)
					.collect(Collectors.joining(StrUtil.COMMA));
		}
		if (e instanceof MethodArgumentNotValidException) {
			// getAllErrors OR getFieldErrors
			message = ((MethodArgumentNotValidException)e).getBindingResult().getFieldErrors().stream()
					.map(DefaultMessageSourceResolvable::getDefaultMessage)
					.collect(Collectors.joining(StrUtil.COMMA));
		}
		if (e instanceof ConstraintViolationException) {
			message = ((ConstraintViolationException)e).getConstraintViolations().stream()
					.map(ConstraintViolation::getMessage)
					.collect(Collectors.joining(StrUtil.COMMA));
		}
		if (e instanceof MissingServletRequestParameterException) {
			MissingServletRequestParameterException exp = ((MissingServletRequestParameterException)e);
			message = "未传" + exp.getParameterName() + "(" + exp.getParameterType() + ")";
		}
		return Result.error(CommonBizExceptionEnum.REQUEST_INVALIDATE, message);
	}

	/**
	 * json parse error
	 * @param e
	 * @return
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = HttpMessageNotReadableException.class)
	public Result<?> httpMessageNotReadableException(HttpMessageNotReadableException e) {
		//便于调试
		e.printStackTrace();
		return Result.error(CommonBizExceptionEnum.JSON_SERIALIZE_EXCEPTION);
	}

	/**
	 * Failed to convert value
	 * @param e
	 * @return
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	@ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
	public Result<?> httpMessageNotReadableException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
		log.debug("接口参数传递类型错误：" + request.getRequestURI());
		e.printStackTrace();//便于调试
		return Result.error(BizConstants.REQ_ERR_CODE, e.getMessage());
	}

	@ExceptionHandler(value = MaxUploadSizeExceededException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<?> handleUploadException(MaxUploadSizeExceededException exp) {
		//便于调试
		exp.printStackTrace();
		if (exp.getCause() instanceof IllegalStateException) {
			IllegalStateException expIll = (IllegalStateException)exp.getCause();
			if (expIll.getCause() instanceof FileSizeLimitExceededException) {
				FileSizeLimitExceededException expFile = (FileSizeLimitExceededException)expIll.getCause();
				String message = "文件超出限制：最大" + BigDecimal.valueOf(expFile.getPermittedSize() / 1024D / 1024D).setScale(2, RoundingMode.HALF_UP) + "MB";
				return Result.error(CommonBizExceptionEnum.REQUEST_INVALIDATE, message);
			}
		}
		if (exp.getMaxUploadSize() != -1) {
			String message = "文件超出限制：最大" + BigDecimal.valueOf(exp.getMaxUploadSize() / 1024D / 1024D).setScale(2, RoundingMode.HALF_UP) + "MB";
			return Result.error(CommonBizExceptionEnum.REQUEST_INVALIDATE, message);
		}
		return Result.error(CommonBizExceptionEnum.REQUEST_INVALIDATE, "文件超出限制");
	}

	/**
	 * 数据库异常
	 * 可以使用instanceOf 记录详细信息
	 * XXX 数据库日志添加
	 * @param e
	 * @return
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = DataAccessException.class)
	public Result<?> dbException(DataAccessException e) {
		e.printStackTrace();
		return Result.builder().enums(DataAccessExceptionEnum.COMMON).devMsgF(e.getMessage()).build();
	}

	/**
	 * 全局异常捕捉处理
	 * XXX 数据库日志添加
	 * @param e
	 * @return
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = Exception.class)
	public Result<?> errorHandler(Exception e) {
		e.printStackTrace();
		return Result.builder().enums(CommonBizExceptionEnum.SERVER_ERROR).devMsgF(e.getMessage()).build();
	}

}
