/**
* Filename    : Compress.java
* Author      : Jack
* Create time : 2015-4-14 下午2:37:29
* Description :
*/
package com.cndw.rpg.framework.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compress
{
  private static int cachesize = 1024;

  public static byte[] compressBytes(byte[] input)
  {
    Deflater compresser = new Deflater();
    compresser.setInput(input);
    compresser.finish();
    byte[] output = new byte[0];
    ByteArrayOutputStream o = new ByteArrayOutputStream(input.length);
    try {
      byte[] buf = new byte[cachesize];

      while (!compresser.finished()) {
        int got = compresser.deflate(buf);
        o.write(buf, 0, got);
      }
      output = o.toByteArray();
    } finally {
      try {
        o.close();
        compresser.end();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return output;
  }

  public static byte[] decompressBytes(byte[] input) {
    byte[] output = new byte[0];

    Inflater decompresser = new Inflater();
    decompresser.setInput(input);
    ByteArrayOutputStream o = new ByteArrayOutputStream(input.length);
    try {
      byte[] buf = new byte[cachesize];

      while (!decompresser.finished()) {
        int got = decompresser.inflate(buf);
        o.write(buf, 0, got);
      }
      output = o.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
      try
      {
        o.close();
        decompresser.end();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    finally
    {
      try
      {
        o.close();
        decompresser.end();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return output;
  }
}
