/**
* Filename    : SocketProxy.java
* Author      : Jack
* Create time : 2015-4-14 下午2:25:32
* Description :
*/
package com.cndw.rpg.net;

import java.net.Socket;

public class SocketProxy extends Socket
{
  private long s;

  public long getS()
  {
    return this.s;
  }

  public void setS(long s) {
    this.s = s;
  }

  public boolean isExpired(long currentTimeMillis) {
    return this.s + 300000L < currentTimeMillis;
  }

  public boolean isFaild(long currentTimeMillis) {
    return (isClosed()) || (isInputShutdown()) || (isOutputShutdown()) || (!isConnected()) || (isExpired(currentTimeMillis));
  }
}
