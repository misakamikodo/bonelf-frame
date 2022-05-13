package com.bonelf.frame.base.util;

import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.core.exception.BonelfException;
import com.bonelf.frame.core.exception.enums.CommonBizExceptionEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 请求创建
 * @author ccy
 * @date 2021-10-11 6:03
 */
public class RequestBuilder {
	private ObjectMapper objectMapper = new ObjectMapper();
	private RestTemplate restTemplate = new RestTemplate();

	private HttpMethod method = HttpMethod.GET;
	/**
	 * 请求流数据
	 */
	private boolean stream;
	/**
	 * 表单提交/urlEncode 提交
	 */
	private boolean formData;
	private HttpEntity<?> formEntity = new HttpEntity<>(
			null, new HttpHeaders());

	/*===========================构造===========================*/

	public RequestBuilder restTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		return this;
	}

	public RequestBuilder formData() {
		this.formData = true;
		return this;
	}

	public RequestBuilder formData(ObjectMapper objectMapper) {
		formData();
		this.objectMapper = objectMapper;
		return this;
	}

	public RequestBuilder method(HttpMethod method) {
		this.method = method;
		return this;
	}

	public RequestBuilder bearerToken(String bearerToken) {
		this.formEntity.getHeaders().add("Authorization", "Bearer " + bearerToken);
		return this;
	}

	public RequestBuilder stream() {
		this.stream = true;
		return this;
	}

	public RequestBuilder json() {
		this.formData = false;
		return this;
	}

	public RequestBuilder noData() {
		return data(null);
	}

	public RequestBuilder data(Object data) {
		HttpHeaders headers = formEntity.getHeaders();
		if (!stream) {
			headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		}
		if (formData && !(data instanceof MultiValueMap)) {
			TypeReference<HashMap<String, Object>> multiTypeReference =
					new TypeReference<HashMap<String, Object>>() {
					};
			MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
			Map<String, Object> map = objectMapper.convertValue(data, multiTypeReference);
			map.forEach(params::add);
			formEntity = new HttpEntity<>(
					params, headers);
		} else {
			formEntity = new HttpEntity<>(
					data, headers);
		}
		return this;
	}

	/*===========================请求===========================*/

	public <RES> Result<RES> send(@NonNull String url,
								  ParameterizedTypeReference<Result<RES>> reference,
								  Object... urlParams) {
		if (formEntity == null) {
			noData();
		}
		ResponseEntity<Result<RES>> data = restTemplate.exchange(
				getSpecificUrl(url),
				method,
				formEntity,
				reference,
				urlParams
		);
		Result<RES> res = data.getBody();
		if (res != null && !res.getSuccess()) {
			throw new BonelfException(res.getCode(), res.getMessage(), res.getDevMessage());
		} else {
			return Optional.ofNullable(res).orElseThrow(() -> new BonelfException(CommonBizExceptionEnum.REQUEST_INVALIDATE));
		}
	}

	public <RES> Result<RES> post(String url, ParameterizedTypeReference<Result<RES>> reference) {
		this.method = HttpMethod.POST;
		return send(url, reference);
	}

	public <RES> Result<RES> get(String url, ParameterizedTypeReference<Result<RES>> reference, Object... urlParams) {
		this.method = HttpMethod.GET;
		return send(url, reference, urlParams);
	}

	private byte[] streamReq(@NonNull String url, Object... urlParams) {
		this.stream = true;
		if (formEntity == null) {
			noData();
		}
		ResponseEntity<byte[]> data = restTemplate.exchange(
				getSpecificUrl(url),
				method,
				formEntity,
				byte[].class,
				urlParams
		);
		return data.getBody();
	}

	public byte[] getStream(String url, Object... urlParams) {
		this.method = HttpMethod.GET;
		return streamReq(url, urlParams);
	}

	private String getSpecificUrl(String url) {
		return url;
	}

}
