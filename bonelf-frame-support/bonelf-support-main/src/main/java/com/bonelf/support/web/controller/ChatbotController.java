package com.bonelf.support.web.controller;

import com.bonelf.frame.base.util.redis.RedisUtil;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.support.web.domain.dto.ChatbotDTO;
import com.bonelf.support.web.domain.vo.ChatbotVO;
import com.bonelf.support.web.service.ChatbotService;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI机器人对话接口
 * @author bonelf
 * @date 2019-10-27
 **/
@Slf4j
@Controller
@Api(value = "AI机器人", tags = {"AI机器人"})
@ApiIgnore
@RequestMapping("/chatbot")
public class ChatbotController {
	@Autowired
	private ChatbotService chatbotService;

	@PostMapping("/chat")
	@ApiOperation(value = "获取验证码")
	public ChatbotVO chat(@RequestBody @Validated ChatbotDTO params) {
		return chatbotService.chat(params);
	}
}
