/**
* Filename    : SimpleRequestHandler.java
* Author      : Jack
* Create time : 2015-4-14 下午2:35:03
* Description :
*/
package com.cndw.rpg.framework.business;

import com.cndw.rpg.exception.StopExecException;
import com.cndw.rpg.framework.Session;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ExceptionEvent;

public class SimpleRequestHandler
  implements RequestHandler
{
  protected final Log log = LogFactory.getLog(getClass());

  public void beforeLogin(Session session)
  {
    InetSocketAddress inetSocketAddress = (InetSocketAddress)session.getChannel().getRemoteAddress();
    String remoteAddress = inetSocketAddress.getAddress().getHostAddress();
    session.setRemoteAddress(remoteAddress);
  }

  public void beforeExecute(Session session)
    throws StopExecException
  {
  }

  public void afterExecute(Session session)
  {
  }

  public void beforeExecuteByUnpack(Session session, Method method, Object[] params)
    throws StopExecException
  {
  }

  public void errOnExecute(Session session, Exception e, Method method, Object[] params)
  {
  }

  public void executeOnChannelClosed(Session session)
  {
  }

  public void errOnMessageReceived(Session session, Exception ex)
  {
  }

  public void errOnCaughtException(Session session, ExceptionEvent e)
  {
  }

  public void errOnResolveParameter(Session session, Exception e, Method method, Object[] params)
  {
  }
}
