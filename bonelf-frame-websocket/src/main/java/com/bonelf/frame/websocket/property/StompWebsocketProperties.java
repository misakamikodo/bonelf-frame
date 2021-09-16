package com.bonelf.frame.websocket.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bonelf.websocket.stomp")
public class StompWebsocketProperties {

	private String endpoint = "wst";

	private String topic = "/topic";

	private String queue = "/queue";

	private String appDestinationPredix = "/app";

	private String userDestinationPredix = "/user";
}
