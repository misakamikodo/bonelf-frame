package com.bonelf.support.web.domain.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * AI聊天
 * @author bonelf
 * @date 2021/6/3 20:21
 */
@Data
@ApiModel("AI聊天返回")
public class ChatbotVO {
	@ApiModelProperty("内容")
	private String content;
}
