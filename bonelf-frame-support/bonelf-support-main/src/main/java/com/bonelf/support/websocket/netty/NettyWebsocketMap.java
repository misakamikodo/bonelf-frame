package com.bonelf.support.websocket.netty;

import com.bonelf.frame.websocket.config.NettyWebsocketConfig;
import com.bonelf.frame.websocket.property.WebsocketProperties;
import com.bonelf.support.websocket.factory.BnfWsMap;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebsocketMap初始化存放地址
 * https://www.cnblogs.com/liangshu/p/12459657.html
 **/
@Component
@ConditionalOnBean(NettyWebsocketConfig.class)
@Slf4j
@ToString
public class NettyWebsocketMap implements BnfWsMap {
	/**
	 * channel payload 携带的 userId 键值
	 */
	public static final AttributeKey<String> USER_ID_CHANNEL_KEY = AttributeKey.valueOf("userId");
	/**
	 * 相当于List<Channel> 但是解决不了userId对应关系,不可能遍历根据AttributeKey获取channel或者是再存对应关系 所以舍弃
	 * 可以自定义channelId实现的，不过使用ChannelGroup也没有优势，这只是简单迭代实现而已
	 * @deprecated
	 */
	@Deprecated
	private ChannelGroup channelGroup;

	@Autowired
	private WebsocketProperties websocketProperties;
	/**
	 * userIdStr：session
	 * 以后扩展群组可以把Channel改成Channel
	 */
	@Getter
	@Setter
	private ConcurrentHashMap<String, Channel> socketSessionMap;

	@PostConstruct
	public void init() {
		log.info("init WebsocketMap for websocket storing userId->channel relation");
		//this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		/*
		 * 根据预估用户量调整这个初始值大小，避免频繁rehash
		 */
		this.socketSessionMap = new ConcurrentHashMap<>(websocketProperties.getInitSessionMapSize());
	}
}
