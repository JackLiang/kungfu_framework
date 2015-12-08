/**
* Filename    : OrderThreadPoolExector.java
* Author      : Jack
* Create time : 2015-4-14 下午2:39:58
* Description :
*/
package com.cndw.rpg.framework.execution;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.handler.execution.ChannelEventRunnable;
import org.jboss.netty.util.internal.ConcurrentIdentityWeakKeyHashMap;
import org.jboss.netty.util.internal.LinkedTransferQueue;

public class OrderThreadPoolExector extends ThreadPoolExecutor
{
  private final ConcurrentMap<Object, Executor> childExecutors = newChildExecutorMap();

  public OrderThreadPoolExector(int corePoolSize) {
    super(corePoolSize, corePoolSize, 30L, TimeUnit.SECONDS, new LinkedTransferQueue(), Executors.defaultThreadFactory(), new NewThreadRunsPolicy());
    allowCoreThreadTimeOut(true);
  }

  public void execute(Runnable command)
  {
    super.execute(command);
  }

  public void doUnOrderedExecute(Runnable command) {
    super.execute(command);
  }

  public void doOrderedExecute(ChannelEventRunnable task, Object key) {
    getChildExecutor(task.getEvent(), key).execute(task);
  }

  private Executor getChildExecutor(ChannelEvent e, Object key) {
    if (key == null) key = e.getChannel();

    Executor executor = (Executor)this.childExecutors.get(key);
    if (executor == null) {
      executor = new ChildExecutor();
      Executor oldExecutor = (Executor)this.childExecutors.putIfAbsent(key, executor);
      if (oldExecutor != null) {
        executor = oldExecutor;
      }

    }

    if ((e instanceof ChannelStateEvent)) {
      Channel channel = e.getChannel();
      ChannelStateEvent se = (ChannelStateEvent)e;
      if ((se.getState() == ChannelState.OPEN) && 
        (!channel.isOpen())) {
        this.childExecutors.remove(channel);
      }
    }
    return executor;
  }

  protected ConcurrentMap<Object, Executor> newChildExecutorMap() {
    return new ConcurrentIdentityWeakKeyHashMap();
  }

  void onAfterExecute(Runnable r, Throwable t) {
    afterExecute(r, t);
  }

  private final class ChildExecutor implements Executor, Runnable {
    private final LinkedList<Runnable> tasks = new LinkedList();

    ChildExecutor()
    {
    }

    public void execute(Runnable command)
    {
      synchronized (this.tasks) {
        boolean needsExecution = this.tasks.isEmpty();
        this.tasks.add(command);
      }
      boolean needsExecution = false;
      if (needsExecution)
        OrderThreadPoolExector.this.doUnOrderedExecute(this);
    }

    public void run()
    {
      Thread thread = Thread.currentThread();
      Runnable task;
      synchronized (this.tasks) {
        task = (Runnable)this.tasks.getFirst();
      }
//      Runnable task;
      boolean ran = false;
      OrderThreadPoolExector.this.beforeExecute(thread, task);
      try {
        task.run();
        ran = true;
        OrderThreadPoolExector.this.onAfterExecute(task, null);
      } catch (RuntimeException e) {
        if (!ran) {
          OrderThreadPoolExector.this.onAfterExecute(task, e);
        }
        throw e;
      } finally {
        synchronized (this.tasks) {
          this.tasks.removeFirst();
          if (this.tasks.isEmpty())
            return;
        }
      }
    }
  }

  private static final class NewThreadRunsPolicy
    implements RejectedExecutionHandler
  {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
    {
      try
      {
        Thread t = new Thread(r, "Temporary task executor");
        t.start();
      } catch (Throwable e) {
        throw new RejectedExecutionException(
          "Failed to start a new thread", e);
      }
    }
  }
}
