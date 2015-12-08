/**
* Filename    : AdminServer.java
* Author      : Jack
* Create time : 2015-4-14 下午2:43:04
* Description :
*/
package com.cndw.rpg.framework.http;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

public class AdminServer
{
  public static void start(int port)
  {
    ServerBootstrap bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(
      Executors.newCachedThreadPool(), 
      Executors.newCachedThreadPool()));

    bootstrap.setPipelineFactory(new ServerPipelineFactory());
//    bootstrap.setPipelineFactory(new ServerPipelineFactory(null));

    bootstrap.bind(new InetSocketAddress(port));
    System.out.println("admin start on " + port);
  }

  private static class ServerPipelineFactory implements ChannelPipelineFactory
  {
    public ChannelPipeline getPipeline() throws Exception
    {
      ChannelPipeline pipeline = Channels.pipeline();
      pipeline.addLast("decoder", new HttpRequestDecoder());
      pipeline.addLast("encoder", new HttpResponseEncoder());

      pipeline.addLast("handler", new AdminServerHandler());
      return pipeline;
    }
  }
}
