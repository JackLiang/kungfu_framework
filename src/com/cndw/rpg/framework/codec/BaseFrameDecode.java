/**
* Filename    : BaseFrameDecode.java
* Author      : Jack
* Create time : 2015-4-14 下午2:36:59
* Description :
*/
package com.cndw.rpg.framework.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class BaseFrameDecode extends FrameDecoder
{
  private static final byte[] FRAME_MAGIC = { 99, 110, 100, 119 };
  private static final int FRAME_HEAD_BODY_SIZE = 4;
  private int frameStatus = 0;
  private int bodySize = 0;
  private static final int FRAME_STATUS_NEED_HEADER = 0;
  private static final int FRAME_STATUS_NEED_BODY = 1;
  private static final int FRAME_STATUS_NEED_CROSS_DOMAIN = 2;
  private static final String CROSS_DOMAIN = "";

  protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
    throws Exception
  {
    switch (this.frameStatus) {
    case 0:
      if (buffer.readableBytes() < FRAME_MAGIC.length + 4) {
        return null;
      }

      byte[] magics = new byte[FRAME_MAGIC.length];
      buffer.readBytes(magics);

      if (!isMagic(magics)) {
        if (isCrossDomain(magics)) {
          ctx.getChannel().write(ChannelBuffers.wrappedBuffer("".getBytes()));

          this.frameStatus = 2;
          return null;
        }if ((isTGWHeaderNew(magics)) || (isTGWHeader(magics))) {
          byte[] tgwHeader = new byte[Math.min(100, buffer.readableBytes())];
          buffer.readBytes(tgwHeader);
          int index = new String(tgwHeader).indexOf("\r\n\r\n");
          buffer.resetReaderIndex();
          if (index < 0) {
            return null;
          }
          buffer.skipBytes(index + 8);
          tgwHeader = null;
          return null;
        }
        throw new CorruptedFrameException("Frame Magic Header Error");
      }

      magics = null;
      this.bodySize = buffer.readInt();
      this.frameStatus = 1;
    case 1:
      if (buffer.readableBytes() < this.bodySize) {
        return null;
      }

      ChannelBuffer frame = buffer.factory().getBuffer(this.bodySize);
      frame.writeBytes(buffer, this.bodySize);
      this.frameStatus = 0;
      return frame;
    case 2:
      return null;
    }

    throw new Error("Decode Frame Status Error");
  }

  private boolean isMagic(byte[] magics)
  {
    return (magics[0] == FRAME_MAGIC[0]) && (magics[1] == FRAME_MAGIC[1]) && (magics[2] == FRAME_MAGIC[2]) && (magics[3] == FRAME_MAGIC[3]);
  }

  private boolean isCrossDomain(byte[] magics) {
    return (magics[0] == 60) && (magics[1] == 112) && (magics[2] == 111) && (magics[3] == 108);
  }

  private boolean isTGWHeader(byte[] magics) {
    return (magics[0] == 71) && (magics[1] == 69) && (magics[2] == 84);
  }

  private boolean isTGWHeaderNew(byte[] magics) {
    return (magics[0] == 116) && (magics[1] == 103) && (magics[2] == 119) && (magics[3] == 95);
  }
}
