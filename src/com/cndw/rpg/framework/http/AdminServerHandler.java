/**
* Filename    : AdminServerHandler.java
* Author      : Jack
* Create time : 2015-4-14 下午2:43:39
* Description :
*/
package com.cndw.rpg.framework.http;

import java.io.PrintStream;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

public class AdminServerHandler extends SimpleChannelUpstreamHandler
{
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
    throws Exception
  {
    HttpRequest request = (HttpRequest)e.getMessage();
    String uriStr = request.getUri();

    System.out.println("uriStr:" + uriStr);
    HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    ChannelBuffer buffer = new DynamicChannelBuffer(2048);
    buffer.writeBytes("hello , http is ok !".getBytes("UTF-8"));

    response.setContent(buffer);
    response.setHeader("Content-Type", "text/html; charset=UTF-8");
    response.setHeader("Content-Length", Integer.valueOf(response.getContent().writerIndex()));
    Channel ch = e.getChannel();

    ch.write(response);
    ch.disconnect();
    ch.close();
  }

  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
    throws Exception
  {
    Channel ch = e.getChannel();
    Throwable cause = e.getCause();
    if ((cause instanceof TooLongFrameException)) {
      sendError(ctx, HttpResponseStatus.BAD_REQUEST);
      return;
    }

    cause.printStackTrace();
    if (ch.isConnected())
      sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
  }

  private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status)
  {
    HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
    response.setHeader("Content-Type", "text/plain; charset=UTF-8");
    response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

    ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
  }
}
