/**
* Filename    : AmfMessageEncode.java
* Author      : Jack
* Create time : 2015-4-14 下午2:36:30
* Description :
*/
package com.cndw.rpg.framework.codec;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Output;
import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;

public class AmfMessageEncode
{
  public static SerializationContext context = new SerializationContext();

  public static ChannelBuffer serialize(Object obj) throws IOException {
    return serializeWithCompress(obj);
  }

  public static ChannelBuffer serialize(Object[] params) throws IOException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    ChannelBufferOutputStream outStream = new ChannelBufferOutputStream(buffer);
    Amf3Output amfOut = new Amf3Output(context);
    amfOut.setOutputStream(outStream);

    Object[] arrayOfObject = params; int j = params.length; for (int i = 0; i < j; i++) { Object obj = arrayOfObject[i];
      amfOut.writeObject(obj);
    }
    amfOut.close();
    return buffer;
  }

  private static ChannelBuffer serializeComm(Object obj) throws IOException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    ChannelBufferOutputStream outStream = new ChannelBufferOutputStream(buffer);
    Amf3Output amfOut = new Amf3Output(context);
    amfOut.setOutputStream(outStream);
    amfOut.writeObject(obj);
    amfOut.close();
    return buffer;
  }

  private static ChannelBuffer serializeWithCompress(Object obj) throws IOException {
    ChannelBuffer amfBuffer = serializeComm(obj);

    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    buffer.writeInt(amfBuffer.readableBytes());

    if (amfBuffer.readableBytes() > 1024) {
      byte[] data = Compress.compressBytes(amfBuffer.array());
      buffer.writeInt(data.length);
      buffer.writeBytes(data);
    } else {
      buffer.writeBytes(amfBuffer);
    }
    return buffer;
  }

  protected static ChannelBuffer serializeWithSize(Object obj) throws IOException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    ChannelBuffer amfBuffer = serializeComm(obj);

    buffer.writeInt(amfBuffer.readableBytes());
    buffer.writeBytes(amfBuffer);

    return buffer;
  }
}
