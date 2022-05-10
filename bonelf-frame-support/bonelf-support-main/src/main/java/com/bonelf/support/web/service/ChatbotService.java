package com.bonelf.support.web.service;


import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
import com.bonelf.support.web.domain.dto.ChatbotDTO;
import com.bonelf.support.web.domain.dto.VerifyCodeDTO;
import com.bonelf.support.web.domain.vo.ChatbotVO;

/**
 * 聊天服务
 */
public interface ChatbotService {
	/**
	 * 聊天
	 * @param params
	 * @return
	 */
	ChatbotVO chat(ChatbotDTO params);
}
