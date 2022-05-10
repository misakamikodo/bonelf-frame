package com.bonelf.support.web.domain.dto;


import com.bonelf.support.constant.CacheConstant;
import com.bonelf.support.constant.QrCodeConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * AI聊天
 * @author bonelf
 * @date 2021/6/3 20:21
 */
@Data
@ApiModel("AI聊天")
public class ChatbotDTO {
	@ApiModelProperty("内容")
	private String content;
}
