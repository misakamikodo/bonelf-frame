package com.bonelf.support.web.domain.dto;


import com.bonelf.support.constant.CacheConstant;
import com.bonelf.support.constant.QrCodeConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * 二维码展示
 * @author bonelf
 * @date 2021/6/3 20:21
 */
@Data
@ApiModel("验证码发送")
public class QrCodeShowDTO {
	// @JsonDeserialize(using = CipherDecryptDeserializer.class)
	@ApiModelProperty("密文")
	private String ticket;

	@ApiParam(value = "过期时间/s", defaultValue = CacheConstant.QR_CODE_EXPIRE_TIME + "", required = false)
	private Long expireTime;

	@ApiParam(value = "宽", defaultValue = QrCodeConstant.QR_CODE_DEFAULT_WIDTH + "", required = false)
	private Integer width = QrCodeConstant.QR_CODE_DEFAULT_WIDTH;

	@ApiParam(value = "高", defaultValue = QrCodeConstant.QR_CODE_DEFAULT_HEIGHT + "", required = false)
	private Integer height = QrCodeConstant.QR_CODE_DEFAULT_HEIGHT;
}
