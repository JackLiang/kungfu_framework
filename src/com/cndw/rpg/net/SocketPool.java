/**
* Filename    : SocketPool.java
* Author      : Jack
* Create time : 2015-4-14 下午2:25:02
* Description :
*/
package com.cndw.rpg.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketPool
{
  protected static final Log log = LogFactory.getLog(SocketPool.class);
  private int serverId;
  private String svrId;
  private String host;
  private int port;
  private InetSocketAddress address;
  private ConcurrentLinkedQueue<SocketProxy> sockets = new ConcurrentLinkedQueue();

  public SocketPool(int serverId, String svrId, String host, int port) {
    this.serverId = serverId;
    this.svrId = svrId;
    this.host = host;
    this.port = port;
  }

  public SocketProxy getConnect() {
    SocketProxy socket = null;
    long s = System.currentTimeMillis();

    while ((!this.sockets.isEmpty()) && (socket == null)) {
      socket = (SocketProxy)this.sockets.poll();
      if ((socket != null) && (socket.isFaild(s))) {
        releaseConnect(socket);
        socket = null;
      }
    }

    if (socket == null) {
      socket = createConnect();
    }
    if (socket != null) {
      socket.setS(s);
    }
    return socket;
  }

  protected void freeConnect(SocketProxy socketProxy) {
    this.sockets.add(socketProxy);
  }

  public void releaseConnect(SocketProxy socketProxy) {
    if (socketProxy != null)
      try {
        socketProxy.close();
      }
      catch (IOException localIOException) {
      }
  }

  public SocketProxy createConnect() {
    SocketProxy socket = new SocketProxy();
    try {
      socket.connect(getAddress(), 2000);
      socket.setSoTimeout(5000);
      socket.setTcpNoDelay(true);
      socket.setKeepAlive(true);
    }
    catch (IOException localIOException) {
    }
    if (this.sockets.size() > 5) {
      log.debug(" create socket on size " + this.sockets.size() + " " + info());
    }
    return socket;
  }

  public final synchronized void clearConnect() {
    while (!this.sockets.isEmpty()) {
      SocketProxy socket = (SocketProxy)this.sockets.poll();
      releaseConnect(socket);
    }
    this.sockets.clear();
  }

  public InetSocketAddress getAddress() {
    if (this.address == null) {
      this.address = new InetSocketAddress(this.host, this.port);
    }
    return this.address;
  }

  public int getServerId() {
    return this.serverId;
  }

  public String getSvrId() {
    return this.svrId;
  }

  public String info() {
    return " id " + this.serverId + " host " + this.host + " port " + this.port;
  }
}
