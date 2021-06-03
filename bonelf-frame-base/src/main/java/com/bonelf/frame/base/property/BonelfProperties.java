package com.bonelf.frame.base.property;

import com.sun.javafx.UnmodifiableArrayList;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "bonelf")
public class BonelfProperties {
	/**
	 * 项目网址
	 */
	private String baseUrl = "http://127.0.0.1:9999";

	private String appName = "bonelf经验总结项目";

	private List<String> noAuthPath = new UnmodifiableArrayList<String>(new String[]{}, 0);
}
