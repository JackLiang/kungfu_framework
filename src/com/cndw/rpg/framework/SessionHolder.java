/**
* Filename    : SessionHolder.java
* Author      : Jack
* Create time : 2015-4-14 下午2:33:28
* Description :
*/
package com.cndw.rpg.framework;

import com.cndw.rpg.framework.group.SessionGroup;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroupFuture;

public class SessionHolder
{
  private static volatile SessionHolder instance;
  private static final String commGroupName = "COMMRADIO";
  private ConcurrentHashMap<Long, Session> HOLDERS = new ConcurrentHashMap();
  private ConcurrentHashMap<String, SessionGroup> CHANNELGROUPS = new ConcurrentHashMap();
  private static Random random = new Random();

  protected final Log log = LogFactory.getLog(getClass());

  public static SessionHolder getInstance()
  {
    if (instance == null) {
      synchronized (SessionHolder.class) {
        if (instance == null) {
          instance = new SessionHolder();
        }
      }
    }
    return instance;
  }

  private SessionGroup getChannelGroup(String idKey)
  {
    if (!this.CHANNELGROUPS.containsKey(idKey)) {
      this.CHANNELGROUPS.putIfAbsent(idKey, new SessionGroup(idKey));
    }
    return (SessionGroup)this.CHANNELGROUPS.get(idKey);
  }

  public void addToRadioGroup(String idKey, Session session)
  {
    SessionGroup myChannelGroup = getChannelGroup(idKey);
    myChannelGroup.add(session.getChannel());
  }

  public void removeFromRadioGroup(String idKey, Session session)
  {
    SessionGroup myChannelGroup = getChannelGroup(idKey);
    myChannelGroup.remove(session.getChannel());
  }

  public Session put(long uid, Session session)
  {
    remove(uid);
    addToRadioGroup("COMMRADIO", session);
    return (Session)this.HOLDERS.put(Long.valueOf(uid), session);
  }

  private void remove(long uid)
  {
    if (this.HOLDERS.containsKey(Long.valueOf(uid))) {
      Session old = (Session)this.HOLDERS.get(Long.valueOf(uid));
      if ((old != null) && (old.getChannel().isOpen())) {
        old.getChannel().close();
      }
      this.HOLDERS.remove(Long.valueOf(uid));
    }
  }

  public boolean remove(long uid, long timeout, TimeUnit unit) {
    if (this.HOLDERS.containsKey(Long.valueOf(uid))) {
      Session old = (Session)this.HOLDERS.get(Long.valueOf(uid));
      if (old != null) {
        if (old.getChannel().isOpen()) {
          old.getChannel().close();
        }
        return old.lock(timeout, unit);
      }
    }
    return true;
  }

  public boolean remove(long uid, Session session)
  {
    if (this.HOLDERS.containsKey(Long.valueOf(uid))) {
      Session old = (Session)this.HOLDERS.get(Long.valueOf(uid));
      if (old != null) {
        if ((session != null) && (!session.equals(old))) {
          return false;
        }
        if (old.getChannel().isOpen()) {
          old.getChannel().close();
        }

        this.HOLDERS.remove(Long.valueOf(uid));
        old.unLock();
      }
    }
    return true;
  }

  public boolean isOnline(long uid)
  {
    return this.HOLDERS.containsKey(Long.valueOf(uid));
  }

  public int onlineNums()
  {
    return this.HOLDERS.size();
  }

  public Set<Long> onlineIds()
  {
    return new HashSet(this.HOLDERS.keySet());
  }

  public long getRandOnLineId() {
    Long[] xxx = (Long[])this.HOLDERS.keySet().toArray(new Long[this.HOLDERS.size()]);
    return xxx[random.nextInt(xxx.length)].longValue();
  }

  public ChannelGroupFuture disconnectComm()
  {
    return getChannelGroup("COMMRADIO").disconnect();
  }

  public void radioGroupMsg(String idKey, ResponseMessage message)
  {
    SessionGroup myChannelGroup = getChannelGroup(idKey);
    try {
      ChannelBuffer buffer = message.serialize();
      myChannelGroup.writeWithNoFuture(buffer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void sendMessage(ResponseMessage msg, Long[] ids)
  {
    try
    {
      if ((ids == null) || (ids.length < 1)) {
        return;
      }
      ChannelBuffer buffer = msg.serialize();
      Long[] arrayOfLong;
      int j = (arrayOfLong = ids).length; for (int i = 0; i < j; i++) { long uid = arrayOfLong[i].longValue();
        write(buffer, uid); }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void sendMessage(ResponseMessage msg, Collection<Long> ids) {
    try {
      if ((ids == null) || (ids.size() < 1)) {
        return;
      }
      ChannelBuffer buffer = msg.serialize();
      for (Long uid : ids)
        write(buffer, uid.longValue());
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void write(ChannelBuffer buffer, long uid)
  {
    Session session = (Session)this.HOLDERS.get(Long.valueOf(uid));
    if (session != null)
      if (session.getChannel().isConnected())
        session.getChannel().write(buffer);
      else
        remove(uid, session);
  }
}
