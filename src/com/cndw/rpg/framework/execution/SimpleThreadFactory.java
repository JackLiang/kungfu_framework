/**
* Filename    : SimpleThreadFactory.java
* Author      : Jack
* Create time : 2015-4-14 下午2:40:29
* Description :
*/
package com.cndw.rpg.framework.execution;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleThreadFactory
  implements ThreadFactory
{
  private final AtomicInteger threadNumber = new AtomicInteger(1);
  private ThreadGroup group;
  private String groupName;

  public SimpleThreadFactory()
  {
    init();
    this.groupName = this.group.getName();
  }

  public SimpleThreadFactory(String groupName) {
    init();
    this.groupName = groupName;
  }

  private void init()
  {
    Executors.defaultThreadFactory();
    SecurityManager securitymanager = System.getSecurityManager();
    this.group = (securitymanager == null ? Thread.currentThread().getThreadGroup() : securitymanager.getThreadGroup());
  }

  public Thread newThread(Runnable runnable)
  {
    String treadName = this.groupName + "-thread-" + this.threadNumber.getAndIncrement();
    Thread t = new Thread(this.group, runnable, treadName);
    if (t.isDaemon())
      t.setDaemon(false);
    if (t.getPriority() != 5)
      t.setPriority(5);
    return t;
  }
}
