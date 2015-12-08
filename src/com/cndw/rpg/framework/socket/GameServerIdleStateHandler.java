/**
* Filename    : GameServerIdleStateHandler.java
* Author      : Jack
* Create time : 2015-4-14 下午2:45:18
* Description :
*/
package com.cndw.rpg.framework.socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.Timer;

public class GameServerIdleStateHandler extends IdleStateHandler
{
  protected final Log log = LogFactory.getLog(getClass());

  public GameServerIdleStateHandler(Timer timer, int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
    super(timer, readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
  }

  protected void channelIdle(ChannelHandlerContext ctx, IdleState state, long lastActivityTimeMillis)
    throws Exception
  {
    if (state == IdleState.READER_IDLE) {
      ctx.getChannel().close();
      this.log.debug("channelIdle read timeout..." + ctx.getChannel().getRemoteAddress().toString() + " | " + lastActivityTimeMillis);
    }
  }
}
