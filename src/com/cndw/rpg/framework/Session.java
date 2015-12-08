/**
* Filename    : Session.java
* Author      : Jack
* Create time : 2015-4-14 下午2:32:58
* Description :
*/
package com.cndw.rpg.framework;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.jboss.netty.channel.Channel;

public class Session
{
  private Channel channel;
  private int status = 0;

  public static int PLAY_STATUS_CONNECTED = 10;

  public static int PLAY_STATUS_AUTHED = 20;

  private long uid = 0L;
  private String accountName;
  private String remoteAddress;
  private ReentrantLock mainLock;
  private Condition termination;

  public Session(Channel channel)
  {
    this.status = PLAY_STATUS_CONNECTED;
    this.channel = channel;
  }

  public long getUid()
  {
    return this.uid;
  }

  public String getAccountName()
  {
    return this.accountName;
  }

  public void setUidAndAccountName(long uid, String accountName)
  {
    if ((this.accountName == null) || (this.uid <= 0L)) {
      this.accountName = accountName;
      this.uid = uid;
    }
  }

  public boolean isLogin()
  {
    return this.status >= PLAY_STATUS_AUTHED;
  }

  public void setStatus(int status)
  {
    this.status = status;
  }

  public Channel getChannel() {
    return this.channel;
  }

  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }

  public String getRemoteAddress()
  {
    return this.remoteAddress;
  }

  protected boolean lock(long timeout, TimeUnit unit) {
    if (timeout <= 0L) {
      return true;
    }
    if ((this.mainLock == null) && (this.termination == null)) {
      synchronized (this) {
        if (this.mainLock == null) {
          this.mainLock = new ReentrantLock();
          this.termination = this.mainLock.newCondition();
        }
      }
    }

    long nanos = unit.toNanos(timeout);
    try
    {
      this.mainLock.lock();
      while (true) {
        if (!SessionHolder.getInstance().isOnline(this.uid)) {
          return true;
        }
        if (nanos <= 0L)
          return false;
        try
        {
          nanos = this.termination.awaitNanos(nanos);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } finally {
      this.mainLock.unlock();
    }
  }

  protected void unLock() {
    if ((this.mainLock != null) && (this.termination != null)) {
      this.mainLock.lock();
      try {
        this.termination.signalAll();
      } finally {
        this.mainLock.unlock();
      }
    }
  }
}
