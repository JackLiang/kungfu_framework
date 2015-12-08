/**
* Filename    : GameServer.java
* Author      : Jack
* Create time : 2015-4-14 下午2:44:28
* Description :
*/
package com.cndw.rpg.framework.socket;

import com.cndw.rpg.framework.business.SimpleRequestHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class GameServer
{
  private int port = 8080;

  public GameServer(int port) {
    this.port = port;
  }

  public void setRequestHandler(SimpleRequestHandler simpleRequestHandler) {
    GameServerHandler.setRequestHandler(simpleRequestHandler);
  }

  public void start()
  {
    ServerBootstrap bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(
      Executors.newCachedThreadPool(), //boss & worker
      Executors.newCachedThreadPool()));

    bootstrap.setOption("child.tcpNoDelay", Boolean.valueOf(true));
    bootstrap.setOption("child.keepAlive", Boolean.valueOf(true));
    bootstrap.setOption("child.reuseAddress", Boolean.valueOf(true));

    bootstrap.setPipelineFactory(new GameServerPipelineFactory());
    bootstrap.bind(new InetSocketAddress(this.port));
  }
}
