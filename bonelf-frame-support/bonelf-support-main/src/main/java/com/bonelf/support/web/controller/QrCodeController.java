package com.bonelf.support.web.controller;

import com.alibaba.fastjson.JSON;
import com.bonelf.cicada.util.CipherCryptUtil;
import com.bonelf.cicada.util.Md5CryptUtil;
import com.bonelf.frame.base.property.BonelfProperties;
import com.bonelf.frame.base.util.JsonUtil;
import com.bonelf.frame.base.util.redis.RedisUtil;
import com.bonelf.frame.cloud.security.constant.AuthFeignConstant;
import com.bonelf.frame.core.constant.AuthConstant;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.support.constant.CacheConstant;
import com.bonelf.support.constant.QrCodeConstant;
import com.bonelf.support.constant.exception.SupportExceptionEnum;
import com.bonelf.support.web.domain.dto.QrCodeShowDTO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码接口
 * @author bonelf
 * @date 2019-10-27
 **/
@Slf4j
@Controller
@Api(value = "二维码API", tags = {"二维码"})
@ApiIgnore
@RequestMapping("/qrcode")
public class QrCodeController {
	@Autowired
	private BonelfProperties bonelfProperties;
	@Autowired
	private RedisUtil redisUtil;
	@Value("${server.servlet.context-path:}")
	private String ctxPath;
	@Autowired
	private TokenStore tokenStore;
	@Autowired
	private RestTemplate restTemplate;

	public static void main(String[] args) throws Exception {
		System.out.println(CipherCryptUtil.encrypt("user/shop?lat=30.00%26lng=120.001", AuthConstant.FRONTEND_PASSWORD_CRYPTO, AuthConstant.FRONTEND_SALT_CRYPTO));
	}

	@GetMapping("/show")
	@ApiOperation(value = "获取二维码")
	public void show(HttpServletResponse response, QrCodeShowDTO qrCodeShowDto) throws Exception {
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE);
		BitMatrix bitMatrix = getBitMatrix(qrCodeShowDto);
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", response.getOutputStream());
	}

	@GetMapping("/base64")
	@ResponseBody
	@ApiOperation(value = "获取Base64二维码")
	public Result<Map<String, Object>> base64(HttpServletResponse response, QrCodeShowDTO qrCodeShowDto) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BitMatrix bitMatrix = getBitMatrix(qrCodeShowDto);
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
		// 将图片转换成base64字符串
		String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
		Map<String, Object> map = new HashMap<>(2);
		map.put("image", "data:image/png;base64," + base64);
		// 缓存到Redis
		return Result.ok(map);
	}

	@GetMapping("/data/{code}")
	@ApiOperation(value = "获取二维码数据")
	public void getData(@RequestHeader(value = AuthConstant.HEADER, required = false) String token,
						HttpServletResponse response,
						@ApiParam(value = "编码", required = true) @PathVariable("code") String code) throws Exception {
		// 如果框架自带了认证失败跳转到登录页或者下载页，这里就不需要
		try {
			if (token == null || tokenStore
					.readAccessToken(token.replace(AuthConstant.TOKEN_PREFIX, ""))
					.isExpired()) {
				// 重定向到项目网页首页或下载页面
				response.sendRedirect(ctxPath + "/download.html");
			}
		} catch (Exception e) {
			// 重定向到项目网页首页或下载页面
			response.sendRedirect(ctxPath + "/download.html");
		}
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8");
		String key = String.format(CacheConstant.QR_CODE_PREFIX, code);
		Object obj = redisUtil.get(key);
		if (obj == null) {
			String uri = null;
			try {
				uri = CipherCryptUtil.decrypt(code,
						AuthConstant.FRONTEND_PASSWORD_CRYPTO, AuthConstant.FRONTEND_SALT_CRYPTO);
			} catch (Exception e) {
				// 解密失败是MD5 有效期加密 返回二维码失效
				obj = Result.error(SupportExceptionEnum.QRCODE_EXPIRE);
			}
			if (uri != null) {
				obj = reqByUri(uri);
			}
		} else {
			// 如果二维码扫了一次就过期则删除redis，不是请注释
			redisUtil.del(key);
		}
		response.getWriter().print(JsonUtil.toJson(obj));
	}

	/**
	 * 获取二维码
	 * @param qrCodeShowDto
	 * @return
	 * @throws Exception
	 */
	private BitMatrix getBitMatrix(QrCodeShowDTO qrCodeShowDto) throws Exception {
		String uri = CipherCryptUtil.decrypt(qrCodeShowDto.getTicket(),
				AuthConstant.FRONTEND_PASSWORD_CRYPTO, AuthConstant.FRONTEND_SALT_CRYPTO);
		Result<?> result = reqByUri(uri);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		String code;
		if (qrCodeShowDto.getExpireTime() != null) {
			code = Md5CryptUtil.encrypt(qrCodeShowDto.getTicket(), AuthConstant.DATABASE_SALT_MD5);
			String key = String.format(CacheConstant.QR_CODE_PREFIX, code);
			redisUtil.set(key, result, Math.min(qrCodeShowDto.getExpireTime(), CacheConstant.QR_CODE_MAX_EXPIRE_TIME));
		} else {
			code = qrCodeShowDto.getTicket();
		}
		return qrCodeWriter.encode(
				bonelfProperties.getBaseUrl() + ctxPath + "/support/qrcode/data/" + code,
				BarcodeFormat.QR_CODE, Math.min(qrCodeShowDto.getWidth(), QrCodeConstant.QR_CODE_MAX_WIDTH),
				Math.min(qrCodeShowDto.getHeight(), QrCodeConstant.QR_CODE_MAX_HEIGHT));
	}


	/**
	 * 请求数据
	 * @param uri
	 * @return
	 */
	private Result<?> reqByUri(String uri) {
		String[] args = uri.split("/");
		String url = "http://" + args[0] + ctxPath + "/" + uri;
		HttpHeaders headers = new HttpHeaders();
		headers.set(AuthFeignConstant.AUTH_HEADER, AuthFeignConstant.FEIGN_REQ_FLAG_PREFIX + " -");
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);
		Result<?> result = Result.error();
		try {
			// 带请求头的getForEntity
			ResponseEntity<Result> resp = restTemplate.exchange(url, HttpMethod.GET, request, Result.class);
			result = resp.getBody();
		} catch (RestClientException e) {
			log.error("数据获取失败", e);
		}
		if (result == null || !result.getSuccess()) {
			log.error("数据获取失败:{}", JSON.toJSONString(result));
		}
		return result;
	}

}
