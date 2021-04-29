package com.bonelf.frame.web.config.swagger;

import com.bonelf.cicada.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.paths.DefaultPathProvider;

@Component
public class SwaggerCloudPathProvider extends DefaultPathProvider {
	@Value("${server.servlet.context-path:}")
	private String ctxPath;
	@Value("${spring.application.name:}")
	private String appName;

	@Override
	public String getOperationPath(String operationPath) {
		//UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/");
		//String uri = Paths.removeAdjacentForwardSlashes(uriComponentsBuilder.path(operationPath).build().toString());
		String uri = super.getOperationPath(operationPath);
		String suffixPath = ctxPath + "/" + StringUtil.crossbarCase2CamelCase(appName);
		return uri.startsWith(ctxPath + "/api") ? uri : uri.replaceFirst(ctxPath, suffixPath);
	}
}
