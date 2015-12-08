/**
* Filename    : SocketRequest.java
* Author      : Jack
* Create time : 2015-4-14 下午2:26:04
* Description :
*/
package com.cndw.rpg.net;

import flex.messaging.io.amf.Amf3Output;
import java.io.IOException;
import java.util.UUID;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;

public class SocketRequest
{
  private String requestId;
  private int serverId;
  private Object[] parameters;
  private String controllerName;
  private String controllerMethod;
  private String cipher;
  private int connetTimeOut = 3000;

  private int readTimeOut = 10000;

  public SocketRequest(int serverId, String controllerName, String controllerMethod, String cipher, Object[] parameters) {
    this.serverId = serverId;
    this.parameters = parameters;
    this.requestId = UUID.randomUUID().toString();
    this.controllerName = controllerName;
    this.controllerMethod = controllerMethod;
    this.cipher = cipher;
  }

  protected ChannelBuffer setRequestHeader(ChannelBuffer buffer)
  {
    ChannelBuffer request = ChannelBuffers.buffer(buffer.readableBytes() + 8);
    request.writeBytes(new byte[] { 99, 110, 100, 119 });
    request.writeInt(buffer.readableBytes());
    request.writeBytes(buffer);
    return request;
  }

  public ChannelBuffer serialize()
  {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    ChannelBufferOutputStream outputStream = new ChannelBufferOutputStream(buffer);
    Amf3Output output = new Amf3Output(SocketClient.context);
    output.setOutputStream(outputStream);
    try
    {
      output.writeObject(this.controllerName);
      output.writeObject(this.controllerMethod);
      output.writeObject(this.requestId);
      output.writeObject(this.cipher);
      if (this.parameters != null)
        for (Object parameter : this.parameters)
          output.writeObject(parameter);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    return setRequestHeader(buffer);
  }

  public String getRequestId() {
    return this.requestId;
  }

  public int getServerId() {
    return this.serverId;
  }

  public String getControllerName() {
    return this.controllerName;
  }

  public String getControllerMethod() {
    return this.controllerMethod;
  }

  public int getConnetTimeOut() {
    return this.connetTimeOut;
  }

  public void setConnetTimeOut(int connetTimeOut) {
    this.connetTimeOut = connetTimeOut;
  }

  public int getReadTimeOut() {
    return this.readTimeOut;
  }

  public void setReadTimeOut(int readTimeOut) {
    this.readTimeOut = readTimeOut;
  }

  public String info() {
    StringBuilder sb = new StringBuilder();
    sb.append(" id ").append(this.serverId);
    sb.append(" c ").append(this.controllerName);
    sb.append(" m ").append(this.controllerMethod);

    return sb.toString();
  }
}
