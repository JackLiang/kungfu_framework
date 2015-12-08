/**
* Filename    : GameServerHandler.java
* Author      : Jack
* Create time : 2015-4-14 下午2:44:55
* Description :
*/
package com.cndw.rpg.framework.socket;

import com.cndw.rpg.exception.StopExecException;
import com.cndw.rpg.framework.Session;
import com.cndw.rpg.framework.business.RequestHandler;
import com.cndw.rpg.framework.business.SimpleRequestHandler;
import com.cndw.rpg.framework.codec.AmfMessageDecode;
import com.cndw.rpg.framework.dispatcher.ControllerDispatcher;
import flex.messaging.io.amf.Amf3Input;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class GameServerHandler extends SimpleChannelUpstreamHandler
{
  protected final Log log = LogFactory.getLog(getClass());
  private Session session;
  private static RequestHandler requestHandler = new SimpleRequestHandler();//请求前验证逻辑

  public static void setRequestHandler(RequestHandler requestHandler) {
    GameServerHandler.requestHandler = requestHandler;
  }

  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    this.session = new Session(ctx.getChannel());
    requestHandler.beforeLogin(this.session);

    super.channelConnected(ctx, e);
  }

  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
  {
    try
    {
      requestHandler.beforeExecute(this.session);//执行前验证

      ChannelBuffer buffer = (ChannelBuffer)e.getMessage();
      ChannelBufferInputStream inputStream = new ChannelBufferInputStream(buffer);//channelBufferInputStream和amf的inputstream互通
      Amf3Input amfInput = new Amf3Input(AmfMessageDecode.context);
      amfInput.setInputStream(inputStream);

      ControllerDispatcher.getInstance().dispatch(requestHandler, this.session, amfInput);//调度封装好的amfInput

      requestHandler.afterExecute(this.session);//执行后验证
    }
    catch (StopExecException localStopExecException)
    {
    }
    catch (Exception ex) {
      requestHandler.errOnMessageReceived(this.session, ex);
    }
    ctx.sendUpstream(e);
  }

  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    requestHandler.executeOnChannelClosed(this.session);
  }

  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
  {
    requestHandler.errOnCaughtException(this.session, e);
  }
}
