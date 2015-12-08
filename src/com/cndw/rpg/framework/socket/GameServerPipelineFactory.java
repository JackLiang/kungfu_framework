/**
 * Filename    : GameServerPipelineFactory.java
 * Author      : Jack
 * Create time : 2015-4-14 下午2:45:43
 * Description :
 */
package com.cndw.rpg.framework.socket;

import com.cndw.rpg.framework.codec.BaseFrameDecode;
import com.cndw.rpg.framework.execution.SimpleThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

public class GameServerPipelineFactory implements ChannelPipelineFactory {

	private static ThreadPoolExecutor executor;
	static {
		ThreadFactory threadFactory = new SimpleThreadFactory("FarmeWorker");
		executor = new OrderedMemoryAwareThreadPoolExecutor(500, 0L, 0L, 60L, TimeUnit.SECONDS, threadFactory);
		executor.allowCoreThreadTimeOut(false);
	}
	private static final ExecutionHandler executionHandler = new ExecutionHandler(executor);
	private static final Timer timer = new HashedWheelTimer(new SimpleThreadFactory("hearbeat"), 1300L,
			TimeUnit.MILLISECONDS, 512);

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("framer", new BaseFrameDecode());
		pipeline.addLast("hearbeat", new GameServerIdleStateHandler(timer, 600, 0, 0));
		pipeline.addLast("thread", executionHandler);
		pipeline.addLast("handler", new GameServerHandler());

		return pipeline;
	}
}
