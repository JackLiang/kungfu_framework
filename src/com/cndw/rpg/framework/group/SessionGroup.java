/**
* Filename    : SessionGroup.java
* Author      : Jack
* Create time : 2015-4-14 下午2:42:19
* Description :
*/
package com.cndw.rpg.framework.group;

import java.net.SocketAddress;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroupFuture;
import org.jboss.netty.util.internal.ConcurrentHashMap;

public class SessionGroup extends AbstractSet<Channel>
  implements ChannelGroup, CustomGroup
{
  private static final AtomicInteger nextId = new AtomicInteger();
  private final String name;
  private final ConcurrentMap<Integer, Channel> nonServerChannels = new ConcurrentHashMap();
  private final ChannelFutureListener remover = new ChannelFutureListener() {
    public void operationComplete(ChannelFuture future) throws Exception {
      SessionGroup.this.remove(future.getChannel());
    }
  };

  public SessionGroup()
  {
    this("group-0x" + Integer.toHexString(nextId.incrementAndGet()));
  }

  public SessionGroup(String name)
  {
    if (name == null) {
      throw new NullPointerException("name");
    }
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public boolean isEmpty()
  {
    return this.nonServerChannels.isEmpty();
  }

  public int size()
  {
    return this.nonServerChannels.size();
  }

  public Channel find(Integer id) {
    return (Channel)this.nonServerChannels.get(id);
  }

  public boolean contains(Object o)
  {
    if ((o instanceof Integer))
      return this.nonServerChannels.containsKey(o);
    if ((o instanceof Channel)) {
      Channel c = (Channel)o;
      return this.nonServerChannels.containsKey(c.getId());
    }
    return false;
  }

  public boolean add(Channel channel)
  {
    boolean added = this.nonServerChannels.putIfAbsent(channel.getId(), channel) == null;
    if (added) {
      channel.getCloseFuture().addListener(this.remover);
    }
    return added;
  }

  public boolean remove(Object o)
  {
    Channel c = null;
    if ((o instanceof Integer)) {
      c = (Channel)this.nonServerChannels.remove(o);
    } else if ((o instanceof Channel)) {
      c = (Channel)o;
      c = (Channel)this.nonServerChannels.remove(c.getId());
    }

    if (c == null) {
      return false;
    }

    c.getCloseFuture().removeListener(this.remover);
    return true;
  }

  public void clear()
  {
    this.nonServerChannels.clear();
  }

  public Iterator<Channel> iterator()
  {
    return this.nonServerChannels.values().iterator();
  }

  public Object[] toArray()
  {
    return this.nonServerChannels.values().toArray();
  }

  public <T> T[] toArray(T[] a)
  {
    return this.nonServerChannels.values().toArray(a);
  }

  public ChannelGroupFuture close() {
    List futures = new ArrayList(size());

    for (Channel c : this.nonServerChannels.values()) {
      futures.add(c.close());
    }

    return new DefaultChannelGroupFuture(this, futures);
  }

  public ChannelGroupFuture disconnect() {
    List futures = new ArrayList(size());
    for (Channel c : this.nonServerChannels.values()) {
      futures.add(c.disconnect());
    }

    return new DefaultChannelGroupFuture(this, futures);
  }

  public ChannelGroupFuture setInterestOps(int interestOps) {
    List futures = new ArrayList(size());
    for (Channel c : this.nonServerChannels.values()) {
      futures.add(c.setInterestOps(interestOps));
    }

    return new DefaultChannelGroupFuture(this, futures);
  }

  public ChannelGroupFuture setReadable(boolean readable) {
    List futures = new ArrayList(size());
    for (Channel c : this.nonServerChannels.values()) {
      futures.add(c.setReadable(readable));
    }
    return new DefaultChannelGroupFuture(this, futures);
  }

  public ChannelGroupFuture unbind() {
    List futures = new ArrayList(size());
    for (Channel c : this.nonServerChannels.values()) {
      futures.add(c.unbind());
    }

    return new DefaultChannelGroupFuture(this, futures);
  }

  public ChannelGroupFuture write(Object message) {
    List futures = new ArrayList(size());
    Channel c;
    if ((message instanceof ChannelBuffer)) {
      ChannelBuffer buf = (ChannelBuffer)message;
      for (Iterator localIterator = this.nonServerChannels.values().iterator(); localIterator.hasNext(); ) { c = (Channel)localIterator.next();
        futures.add(c.write(buf.duplicate())); }
    }
    else {
      for (Channel c1 : this.nonServerChannels.values()) {
        futures.add(c1.write(message));
      }
    }
    return new DefaultChannelGroupFuture(this, futures);
  }

  public void writeWithNoFuture(Object message)
  {
    for (Channel c : this.nonServerChannels.values())
      c.write(message);
  }

  public ChannelGroupFuture write(Object message, SocketAddress remoteAddress)
  {
    List futures = new ArrayList(size());
    Channel c;
    if ((message instanceof ChannelBuffer)) {
      ChannelBuffer buf = (ChannelBuffer)message;
      for (Iterator localIterator = this.nonServerChannels.values().iterator(); localIterator.hasNext(); ) { c = (Channel)localIterator.next();
        futures.add(c.write(buf.duplicate(), remoteAddress)); }
    }
    else {
      for (Channel c2 : this.nonServerChannels.values()) {
        futures.add(c2.write(message, remoteAddress));
      }
    }
    return new DefaultChannelGroupFuture(this, futures);
  }

  public int hashCode()
  {
    return System.identityHashCode(this);
  }

  public boolean equals(Object o)
  {
    return this == o;
  }

  public int compareTo(ChannelGroup o) {
    int v = getName().compareTo(o.getName());
    if (v != 0) {
      return v;
    }

    return System.identityHashCode(this) - System.identityHashCode(o);
  }

  public String toString()
  {
    return getClass().getSimpleName() + "(name: " + getName() + ", size: " + size() + ')';
  }
}
