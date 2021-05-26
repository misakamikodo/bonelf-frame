/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.bonelf.frame.websocket.property;

import com.bonelf.frame.websocket.property.enums.TopicType;
import com.bonelf.frame.websocket.property.enums.WebsocketType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@ConfigurationProperties(prefix = "bonelf.websocket")
public class WebsocketProperties {

	/**
	 * 使用redis消息订阅分渠道
	 */
	private WebsocketType type = WebsocketType.stomp;

	/**
	 * 消息发放方式
	 */
	private TopicType topicType = TopicType.norm;

	/**
	 * 会话人数Map初始大小
	 * 对于netty和norm类型的websocket配置
	 */
	private Integer initSessionMapSize = 1000;

	/**
	 * mq topicType 主题
	 */
	private String mqTopic = "websocketTopic";

	/**
	 * 所有渠道
	 * 在不使用Mq、stomp主动获取的情况（见 link）下，由support主动将消息通过 feign或者redis发布订阅 发给服务 需要配置cmdMap 来处理指令消息和处理的服务对应关系，各服务需提供接收消息接口（frame-websocket已实现）。
	 * 使用 Mq 通过订阅websocketTopic主题下的tag(@StreamListener)来实现主动接收，
	 * 使用 stomp 则通过 @MessageMapping 来实现主动接收，推荐使用stomp结合Mq的方式
	 */
	@Deprecated
	private String[] channels;

	/**
	 * 命令-渠道 关系
	 */
	@Deprecated
	private Map<String, String[]> cmdChannels;

	@Deprecated
	@JsonIgnore
	private transient Map<String, Map<String, Object>> cmdIdTopicMap;

	@PostConstruct
	public void init(){
		this.cmdChannels = new HashMap<>();
		if (cmdIdTopicMap == null) {
			return;
		}
		cmdIdTopicMap.forEach((key, value)->{
			Set<String> result = new HashSet<>();
			for (Map.Entry<String, Object> entry : value.entrySet()) {
				if ("exclude".equals(entry.getKey())) {
					List<String> channels = new ArrayList<>(Arrays.asList(Optional.ofNullable(getChannels()).orElse(new String[]{})));
					Map<String, String> map = (Map<String, String>)entry.getValue();
					map.forEach((index, item) -> channels.removeIf(i->i.equals(item)));
					result.addAll(channels);
					break;
				} else if ("include".equals(entry.getKey())) {
					Map<String, String> map = (Map<String, String>)entry.getValue();
					map.forEach((index, item) -> result.add(item));
					break;
				} else {
					result.add((String)entry.getValue());
				}
			}
			this.cmdChannels.put(key, result.toArray(new String[0]));
		});
	}

	public String getMqTopic() {
		return mqTopic;
	}

	public void setMqTopic(String mqTopic) {
		this.mqTopic = mqTopic;
	}

	public Map<String, String[]> getCmdChannels() {
		return cmdChannels;
	}

	public void setCmdChannels(Map<String, String[]> cmdChannels) {
		this.cmdChannels = cmdChannels;
	}

	public void setCmdIdTopicMap(Map<String, Map<String, Object>> cmdIdTopicMap) {
		this.cmdIdTopicMap = cmdIdTopicMap;
	}

	public WebsocketType getType() {
		return type;
	}

	public void setType(WebsocketType type) {
		this.type = type;
	}

	public TopicType getTopicType() {
		return topicType;
	}

	public void setTopicType(TopicType topicType) {
		this.topicType = topicType;
	}

	public Integer getInitSessionMapSize() {
		return initSessionMapSize;
	}

	public void setInitSessionMapSize(Integer initSessionMapSize) {
		this.initSessionMapSize = initSessionMapSize;
	}

	public String[] getChannels() {
		return channels;
	}

	public void setChannels(String[] channels) {
		this.channels = channels;
	}
}
