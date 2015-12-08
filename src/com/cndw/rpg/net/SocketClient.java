/**
* Filename    : SocketClient.java
* Author      : Jack
* Create time : 2015-4-14 下午2:23:56
* Description :
*/
package com.cndw.rpg.net;

import com.cndw.rpg.framework.ResponseMessage;
import com.cndw.rpg.framework.codec.Compress;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;

public class SocketClient
{
  protected static final Log log = LogFactory.getLog(SocketClient.class);

  private static final Map<Integer, SocketPool> allSocketClients = new ConcurrentHashMap();

  public static SerializationContext context = new SerializationContext();

  static {
    context.legacyCollection = true;
  }

  public static void addClient(int serverId, String svrId, String host, int port)
  {
    SocketPool socketClient = new SocketPool(serverId, svrId, host, port);
    allSocketClients.put(Integer.valueOf(serverId), socketClient);
  }

  public static SocketPool getSocketClient(int serverId) {
    return (SocketPool)allSocketClients.get(Integer.valueOf(serverId));
  }

  public static boolean contains(int serverId) {
    return allSocketClients.containsKey(Integer.valueOf(serverId));
  }

  public static ResponseMessage sendRequest(SocketRequest request) {
    if (!contains(request.getServerId())) {
      log.error("no client " + request.info());
      return null;
    }
    SocketPool socketPool = getSocketClient(request.getServerId());
    SocketProxy socket = null;
    try {
      socket = socketPool.getConnect();
      if (socket.getSoTimeout() != request.getReadTimeOut()) {
        socket.setSoTimeout(request.getReadTimeOut());
      }
      socket.getOutputStream().write(request.serialize().array());
      socket.getOutputStream().flush();
      Amf3Input input = new Amf3Input(context);
      InputStream stream = socket.getInputStream();
      ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

      int bodySize = 0;
      int compressSize = 0;
      byte[] bytes = new byte[1024];
      do
      {
        int length = stream.read(bytes);

        if (length != -1) {
          buffer.writeBytes(bytes, 0, length);
        }

        while ((bodySize == 0) && (buffer.readableBytes() >= 4)) {
          bodySize = buffer.readInt();
          if ((bodySize > 1024) && (compressSize == 0)) {
            compressSize = bodySize;
            bodySize = 0;
          }
        }
      }

      while (buffer.readableBytes() < bodySize);

      ChannelBuffer bodyBuffer = null;
      if (compressSize > 0) {
        byte[] cpomressBytes = Compress.decompressBytes(buffer.readBytes(bodySize).array());
        bodyBuffer = buffer.factory().getBuffer(cpomressBytes.length);
        bodyBuffer.writeBytes(cpomressBytes);
      } else {
        bodyBuffer = buffer.factory().getBuffer(bodySize);
        bodyBuffer.writeBytes(buffer, bodySize);
      }

      input.setInputStream(new ChannelBufferInputStream(bodyBuffer));

      ResponseMessage message = (ResponseMessage)input.readObject();
      input.close();
      return message;
    } catch (IOException e) {
      socketPool.releaseConnect(socket);
      socket = null;
      log.error("Socket err:" + e.getClass().getSimpleName() + " > " + e.getMessage() + " ->" + request.info());
    }
    catch (Exception e) {
      socketPool.releaseConnect(socket);
      socket = null;
      e.printStackTrace();
    } finally {
      if (socket != null) {
        socketPool.freeConnect(socket);
      }
    }
    return null;
  }
}