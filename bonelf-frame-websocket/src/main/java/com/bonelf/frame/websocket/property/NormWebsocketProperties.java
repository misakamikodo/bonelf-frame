package com.bonelf.frame.websocket.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bonelf.websocket.norm")
public class NormWebsocketProperties {

	private String endpoint = "/wst";

}
