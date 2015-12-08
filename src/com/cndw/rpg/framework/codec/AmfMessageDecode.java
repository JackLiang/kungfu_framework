/**
* Filename    : AmfMessageDecode.java
* Author      : Jack
* Create time : 2015-4-14 下午2:35:53
* Description :
*/
package com.cndw.rpg.framework.codec;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;
import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;

public class AmfMessageDecode
{
  public static SerializationContext context = new SerializationContext();

  public static Object deSerialize(ChannelBuffer buffer) throws ClassNotFoundException, IOException { ChannelBufferInputStream inputStream = new ChannelBufferInputStream(buffer);
    Amf3Input amfIn = new Amf3Input(context);
    amfIn.setInputStream(inputStream);

    return amfIn.readObject(); }

  public static Object[] deSerialize(Class<?>[] types, ChannelBuffer buffer) throws IOException, ClassNotFoundException
  {
    ChannelBufferInputStream inputStream = new ChannelBufferInputStream(buffer);
    return deSerialize(types, inputStream);
  }

  public static Object[] deSerialize(Class<?>[] types, ChannelBufferInputStream inputStream) throws IOException, ClassNotFoundException {
    Amf3Input amfIn = new Amf3Input(context);
    amfIn.setInputStream(inputStream);

    return deSerialize(types, amfIn);
  }

  /**反序列化
 * @param types
 * @param amfIn
 * @return
 * @throws IOException
 * @throws ClassNotFoundException
 */
public static Object[] deSerialize(Class<?>[] types, Amf3Input amfIn) throws IOException, ClassNotFoundException {
    Object[] args = new Object[types.length];
    if (types.length > 1) {
      for (int i = 1; i < types.length; i++) {
        Object cArg = amfIn.readObject();

        if ((cArg instanceof Double)) {
          Double tmpLong = (Double)cArg;
          cArg = Long.valueOf(tmpLong.longValue());
        }
        args[i] = cArg;
      }
    }
    return args;
  }
}