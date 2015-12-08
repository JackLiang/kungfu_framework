/**
* Filename    : GameClientTest.java
* Author      : Jack
* Create time : 2015-4-14 下午2:31:24
* Description :
*/
package com.cndw.rpg.test;

import com.cndw.rpg.framework.codec.AmfMessageEncode;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class GameClientTest
{
  public static void main(String[] args)
    throws IOException
  {
    ClientBootstrap bootstrap = new ClientBootstrap(
      new NioClientSocketChannelFactory(
      Executors.newCachedThreadPool(), 
      Executors.newCachedThreadPool()));

    ChannelFuture future = bootstrap.connect(new InetSocketAddress("localhost", 7776));
    Channel channel = future.awaitUninterruptibly().getChannel();
    if (!future.isSuccess()) {
      future.getCause().printStackTrace();
      bootstrap.releaseExternalResources();
      return;
    }

    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    buffer.writeByte(99);
    buffer.writeByte(110);
    buffer.writeByte(100);
    buffer.writeByte(119);

    writeBindString(buffer, "sence");

    ChannelBuffer amfBuffer = buildAmfMessage();
    buffer.writeInt(amfBuffer.readableBytes());
    buffer.writeBytes(amfBuffer);

    future = channel.write(buffer);

    if (future.isSuccess()) System.exit(0);
    future.awaitUninterruptibly();
    bootstrap.releaseExternalResources();
  }

  private static void writeBindString(ChannelBuffer buffer, String s) {
    if (s == null) {
      buffer.writeByte(0);
      return;
    }

    byte[] sb = s.getBytes();
    byte sl = (byte)(0xFFFFFF80 | (byte)sb.length);
    buffer.writeByte(sl);
    buffer.writeBytes(sb);
  }

  private static ChannelBuffer buildAmfMessage() throws IOException {
    return AmfMessageEncode.serialize(new Object[] { "LoginController", "test", "zhangjie" });
  }
}
