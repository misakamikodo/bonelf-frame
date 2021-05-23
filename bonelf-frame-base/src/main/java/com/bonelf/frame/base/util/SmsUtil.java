package com.bonelf.frame.base.util;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.bonelf.frame.base.property.AliSmsProperties;
import com.bonelf.frame.core.constant.AliSmsTemplateCode;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 发送验证码工具
 * @author chuanfu
 * @version v1.0
 * @date 2019-10-10
 */
@Slf4j
@Component
public class SmsUtil {
	public static final String SEND_OK = "OK";
	@Autowired
	private AliSmsProperties smsProperty;

	/**
	 * 短信发送
	 * @param telephone 发送号码
	 * @param code      验证码
	 * @author chuanfu
	 * @date 2019-3-23
	 */
	public Map<String, Object> sendVerify(String telephone, String code) {
		DefaultProfile profile = DefaultProfile.getProfile(smsProperty.getRegionId(), smsProperty.getAccessKeyId(), smsProperty.getAccessSecret());
		IAcsClient client = new DefaultAcsClient(profile);
		CommonRequest request = new CommonRequest();
		request.setSysMethod(MethodType.POST);
		request.setSysDomain("dysmsapi.aliyuncs.com");
		request.setSysVersion(smsProperty.getVersion());
		request.setSysAction("SendSms");
		request.putQueryParameter("RegionId", smsProperty.getRegionId());
		request.putQueryParameter("PhoneNumbers", telephone);
		request.putQueryParameter("SignName", smsProperty.getSignName());
		request.putQueryParameter("TemplateCode", AliSmsTemplateCode.VERIFY);
		request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");
		CommonResponse response;
		try {
			response = client.getCommonResponse(request);
			Map<String, Object> data = JsonUtil.toMap(response.getData());
			if (!SEND_OK.equals(data.get("Message"))) {
				log.debug("短信发送失败:\n" + JsonUtil.toJson(response));
			}
			return data;
		} catch (ClientException e) {
			e.printStackTrace();
			throw BonelfException.builder().enums(CommonBizExceptionEnum.THIRD_FAIL)
					.devMsgF(e.getErrCode()).build();
		}

	}
}
