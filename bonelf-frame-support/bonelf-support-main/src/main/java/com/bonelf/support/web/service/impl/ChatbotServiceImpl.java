package com.bonelf.support.web.service.impl;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.mail.MailAccount;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.bonelf.frame.base.property.AliCharbotProperties;
import com.bonelf.frame.base.property.BonelfMailProperties;
import com.bonelf.frame.base.property.BonelfProperties;
import com.bonelf.frame.base.util.redis.RedisUtil;
import com.bonelf.frame.core.auth.constant.VerifyCodeTypeEnum;
import com.bonelf.frame.core.constant.BonelfConstant;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.support.web.domain.dto.ChatbotDTO;
import com.bonelf.support.web.domain.vo.ChatbotVO;
import com.bonelf.support.web.service.ChatbotService;
import com.bonelf.support.web.service.MailService;
import com.google.gson.JsonArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatbotServiceImpl implements ChatbotService {
	@Autowired
	private AliCharbotProperties charbotProperties;

	@Override
	public ChatbotVO chat(ChatbotDTO params) {
		String accountAccessAK = charbotProperties.getAccessKeyId();
		String accountAccessSK = charbotProperties.getAccessSecret();
		String popRegion = charbotProperties.getRegion();
		String popProduct = charbotProperties.getProduct();
		String popDomain = charbotProperties.getPopDomain();
		DefaultProfile.addEndpoint(popRegion, popProduct, popDomain);
		IClientProfile profile = DefaultProfile.getProfile(popRegion, accountAccessAK, accountAccessSK);
		DefaultAcsClient client = new DefaultAcsClient(profile);
		//固定入参
		CommonRequest commonRequest = new CommonRequest();
		commonRequest.setSysProduct(charbotProperties.getProduct());
		commonRequest.setSysMethod(MethodType.GET);
		//根据API会有变化
		commonRequest.setSysAction(charbotProperties.getAction());
		commonRequest.setSysVersion(charbotProperties.getSysVersion());
		commonRequest.putQueryParameter("Utterance", params.getContent());
		//机器人id
		commonRequest.putQueryParameter("InstanceId", charbotProperties.getDefaultInstanceId());
		String resp;
		try {
			CommonResponse commonResponse = client.getCommonResponse(commonRequest);
			resp = commonResponse.getData();
		} catch (ClientException e) {
			throw new BonelfException("请求异常");
		}
		/*
		{"Messages":
		[{"Type":"Text","AnswerType":"Text","Text":{"Ext":{},"ContentType":"PLAIN_TEXT","UserDefinedChatTitle":"",
		"AnswerSource":"ChitChat","Content":"我一直在这里呀","HitStatement":"hi"},"Knowledge":{}}],
		"RequestId":"18823610-0775-50D2-8945-F95A44DA3BCD","SessionId":"e1074d3e897e44dc83d5ee9d2250a62c",
		"MessageId":"18823610-0775-50D2-8945-F95A44DA3BCD"}
		 */
		JSONObject respJson = JSON.parseObject(resp);
		ChatbotVO result = new ChatbotVO();
		String content = "";
		JSONArray messageArr = respJson.getJSONArray("Messages");
		if (messageArr != null && messageArr.size() > 0) {
			JSONObject message = (JSONObject)messageArr.get(0);
			JSONObject textObj = message.getJSONObject("Text");
			if (textObj != null) {
				content = textObj.getString("Content");
			}
		}
		result.setContent(content);
		return result;
	}
}
