package com.bonelf.frame.core.domain;

import com.bonelf.frame.core.constant.BizConstants;
import com.bonelf.frame.core.dict.enums.DictField;
import com.bonelf.frame.core.exception.AbstractBaseExceptionEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Optional;

/**
 * 接口返回数据格式
 * TODO 构造器
 * @author bonelf
 * @date 2019年1月19日
 */
@Data
@NoArgsConstructor
@ApiModel(value = "接口返回对象", description = "接口返回对象")
public class Result<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 成功标志
	 */
	@ApiModelProperty(value = "成功标志")
	private Boolean success;

	/**
	 * 返回处理消息
	 */
	@ApiModelProperty(value = "异常信息")
	private String devMessage;

	/**
	 * 返回处理消息
	 */
	@ApiModelProperty(value = "返回处理消息 弹窗")
	private String message;

	/**
	 * 返回代码
	 */
	@ApiModelProperty(value = "返回状态码")
	private String code;

	/**
	 * 返回数据对象 data
	 */
	@DictField
	@ApiModelProperty(value = "返回数据对象")
	private T result;

	/**
	 * 时间戳
	 */
	@ApiModelProperty(value = "时间戳")
	private Long timestamp;
	/**
	 * 接口耗时
	 * TODO aop/拦截器
	 */
	@ApiModelProperty(value = "接口调用耗时：ms")
	private Long cost;

	/*===========================构造器===========================*/

	public static class Builder<T> {
		private Result<T> result;

		public Builder() {
			result = new Result<>();
			this.result.timestamp = System.currentTimeMillis();
		}

		/*===========================枚举构造===========================*/

		public Builder<T> enums(AbstractBaseExceptionEnum exception) {
			this.result.message = exception.getMessage();
			this.result.devMessage = exception.getDevMessage();
			this.result.code = exception.getCode();
			this.result.success = isValidCode(exception.getCode());
			return this;
		}

		/**
		 * format
		 * @param msgs
		 * @return
		 */
		public Builder<T> msgF(Object... msgs) {
			this.result.message = String.format(Optional.ofNullable(this.result.message).orElse("%s"), (Object[])msgs);
			return this;
		}

		/**
		 * format
		 * @param devMsgs
		 * @return
		 */
		public Builder<T> devMsgF(Object... devMsgs) {
			this.result.devMessage = String.format(Optional.ofNullable(this.result.devMessage).orElse("%s"), (Object[])devMsgs);
			return this;
		}

		/*===========================编码构造===========================*/

		public Builder<T> code(String code) {
			this.result.code = code;
			this.result.success = isValidCode(code);
			return this;
		}

		public Builder<T> msg(String msg) {
			this.result.message = msg;
			return this;
		}

		public Builder<T> devMsg(String devMsg) {
			this.result.devMessage = devMsg;
			return this;
		}

		public Builder<T> result(T result) {
			this.result.result = result;
			return this;
		}

		public Result<T> build() {
			this.result.timestamp = System.currentTimeMillis();
			return this.result;
		}
	}

	public static <T> Builder<T> builder() {
		return new Builder<T>();
	}

	/*===========================快速构造===========================*/

	public Result<T> success(String code, String message) {
		this.message = message;
		this.code = code;
		this.success = true;
		this.timestamp = System.currentTimeMillis();
		return this;
	}

	public Result<T> success(String message) {
		return this.success(BizConstants.CODE_200, message);
	}

	public Result<T> error500(String message) {
		this.message = message;
		this.code = BizConstants.CODE_500;
		this.success = false;
		this.timestamp = System.currentTimeMillis();
		return this;
	}

	/*===========================静态方法===========================*/

	public static <T> Result<T> ok() {
		return ok(null);
	}


	public static <T> Result<T> ok(String msg, T data) {
		Result<T> r = ok(data);
		r.setMessage(msg);
		return r;
	}

	/**
	 * 消息弹窗通知
	 * @param msg 弹窗消息
	 * @param <T> 消息类型
	 * @return
	 */
	public static <T> Result<T> okMsg(String msg) {
		Result<T> r = new Result<>();
		r.setSuccess(true);
		r.setCode(BizConstants.CODE_200);
		r.setTimestamp(System.currentTimeMillis());
		r.setMessage(msg);
		return r;
	}

	/**
	 * 消息弹窗通知
	 * @param msg 弹窗消息
	 * @param debugResult 错误消息
	 * @param <T> 消息类型
	 * @return
	 */
	public static <T> Result<T> okMsg(String msg, T debugResult) {
		Result<T> r = okMsg(msg);
		r.setResult(debugResult);
		return r;
	}

	public static <T> Result<T> ok(T data) {
		Result<T> r = new Result<>();
		r.setSuccess(true);
		r.setCode(BizConstants.CODE_200);
		r.setTimestamp(System.currentTimeMillis());
		r.setResult(data);
		return r;
	}

	public static <T> Result<T> error(String msg) {
		return error(BizConstants.CODE_500, msg);
	}

	public static <T> Result<T> error() {
		return error(BizConstants.CODE_500, "fail");
	}

	public static <T> Result<T> error(String code, String msg) {
		Result<T> r = new Result<T>();
		r.setCode(code);
		r.setMessage(msg);
		r.setSuccess(false);
		r.setTimestamp(System.currentTimeMillis());
		return r;
	}

	public static <T> Result<T> error(AbstractBaseExceptionEnum exception) {
		return error(exception.getCode(), exception.getMessage());
	}

	/*===========================devMsg 异常调用，给开发的消息===========================*/

	public static <T> Result<T> errorDev(String devMsg) {
		return errorDev(BizConstants.CODE_500, devMsg);
	}

	public static <T> Result<T> errorDev() {
		return errorDev(BizConstants.CODE_500, "fail");
	}

	public static <T> Result<T> errorDev(String code, String devMsg) {
		Result<T> r = new Result<>();
		r.setCode(code);
		r.setDevMessage(devMsg);
		r.setSuccess(false);
		r.setTimestamp(System.currentTimeMillis());
		return r;
	}

	public static <T> Result<T> errorDev(AbstractBaseExceptionEnum exception) {
		return errorDev(exception.getCode(), exception.getMessage());
	}

	public static <T> Result<T> error(AbstractBaseExceptionEnum exception, Object... format) {
		return error(exception.getCode(), String.format(exception.getMessage(), (Object[])format));
	}

	/*===========================私有方法===========================*/

	private static boolean isValidCode(String code) {
		return code.length() == 3 && code.startsWith("2");
	}
}