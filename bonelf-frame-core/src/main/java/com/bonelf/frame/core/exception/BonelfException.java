package com.bonelf.frame.core.exception;

import com.bonelf.frame.core.constant.BizConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * <p>
 * 服务异常
 * </p>
 * @author bonelf
 * @since 2020/10/11 17:34
 */
@Getter
@Setter
@AllArgsConstructor
public class BonelfException extends RuntimeException {
	private String code;
	private String errorMessage;
	private String devMessage;

	/*===========================构造器===========================*/

	public static class Builder {
		private BonelfException exception;

		public Builder() {
			exception = new BonelfException();
		}

		public Builder(String msg) {
			exception = new BonelfException(msg);
		}

		/*===========================枚举构造===========================*/

		public Builder enums(AbstractBaseExceptionEnum exception) {
			this.exception.errorMessage = exception.getMessage();
			this.exception.devMessage = exception.getDevMessage();
			this.exception.code = exception.getCode();
			return this;
		}

		public Builder msgF(Object... msgs) {
			this.exception.errorMessage = String.format(Optional.ofNullable(this.exception.errorMessage).orElse("%s"), (Object[])msgs);
			return this;
		}

		public Builder devMsgF(Object... devMsgs) {
			this.exception.devMessage = String.format(Optional.ofNullable(this.exception.devMessage).orElse("%s"), (Object[])devMsgs);
			return this;
		}

		/*===========================编码构造===========================*/

		public Builder code(String code) {
			this.exception.code = code;
			return this;
		}

		public Builder msg(String msg) {
			this.exception.errorMessage = msg;
			return this;
		}

		public Builder devMsg(String devMsg) {
			this.exception.devMessage = devMsg;
			return this;
		}

		public BonelfException build() {
			return this.exception;
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(String msg) {
		return new Builder(msg);
	}
	
	/*===========================END===========================*/
	

	private BonelfException() {
	}

	public BonelfException(Exception e) {
		super(e.getMessage());
		this.code = BizConstants.CODE_500;
		this.errorMessage = e.getMessage();
	}

	public BonelfException(String messageFor500) {
		super(messageFor500);
		this.code = BizConstants.CODE_500;
		this.errorMessage = messageFor500;
	}

	public BonelfException(AbstractBaseExceptionEnum exception) {
		super(exception.getMessage());
		this.code = exception.getCode();
		this.errorMessage = exception.getMessage();
	}

	public BonelfException(AbstractBaseExceptionEnum exception, Object... format) {
		super(String.format(exception.getMessage(), (Object[])format));
		this.code = exception.getCode();
		this.errorMessage = String.format(exception.getMessage(), (Object[])format);
	}

	public String getCode() {
		return code;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getDevMessage() {
		return devMessage;
	}
}
