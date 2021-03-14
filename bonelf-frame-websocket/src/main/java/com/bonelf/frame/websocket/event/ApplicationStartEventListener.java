package com.bonelf.frame.websocket.event;

import cn.hutool.core.thread.ThreadUtil;
import com.bonelf.frame.websocket.config.NettyWebsocketConfig;
import com.bonelf.frame.websocket.netty.NettyWebsocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * <p>
 * 也可以使用 implements ApplicationRunner
 * </p>
 * @author bonelf
 * @since 2020/10/18 16:16
 */
@Slf4j
@ConditionalOnBean(NettyWebsocketConfig.class)
public class ApplicationStartEventListener implements ApplicationListener<ContextRefreshedEvent> {
	@Autowired
	private NettyWebsocketServer nettyWebsocketServer;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		//ws会堵塞导致后面代码不执行
		ThreadUtil.execAsync(() -> {
			try {
				nettyWebsocketServer.run();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}

}


