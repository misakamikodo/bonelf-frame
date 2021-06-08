package com.bonelf.support.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * FTP
 * @author ccy
 * @date 2021/6/7 15:51
 */
@Data
@Component
@ConfigurationProperties(prefix = "bonelf.upload.dtp")
public class BonelfFtpProperties {
	/**
	 * id
	 */
	private String username = "ftp";
	/**
	 * 秘钥
	 */
	private String password = "bonelf@123";
	/**
	 * ip
	 */
	private String ip = "ftp.bonelf.com";
	/**
	 * 端口
	 */
	private Integer port = 21;
	/**
	 * 节点
	 */
	private String endpoint = "http://" + ip;
	/**
	 * bucket
	 */
	private String bucket = "bonelf";
}
