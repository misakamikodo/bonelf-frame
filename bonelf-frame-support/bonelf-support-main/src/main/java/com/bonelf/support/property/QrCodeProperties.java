package com.bonelf.support.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "bonelf.qrcode")
public class QrCodeProperties {
	/**
	 * 永久有效的请求
	 */
	private Map<String, String> ticketUri = new HashMap<>();
	/**
	 * 会过期的请求
	 */
	private Map<String, String> limitTicketUri = new HashMap<>();

	/**
	 * 不用认证的路径
	 */
	private List<String> permitUrls = new ArrayList<>();
}
