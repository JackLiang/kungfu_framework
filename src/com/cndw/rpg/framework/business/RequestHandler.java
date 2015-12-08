/**
* Filename    : RequestHandler.java
* Author      : Jack
* Create time : 2015-4-14 下午2:34:34
* Description :
*/
package com.cndw.rpg.framework.business;

import com.cndw.rpg.exception.StopExecException;
import com.cndw.rpg.framework.Session;
import java.lang.reflect.Method;
import org.jboss.netty.channel.ExceptionEvent;

public abstract interface RequestHandler
{
  public abstract void beforeLogin(Session paramSession);

  public abstract void beforeExecute(Session paramSession)
    throws StopExecException;

  public abstract void afterExecute(Session paramSession);

  public abstract void beforeExecuteByUnpack(Session paramSession, Method paramMethod, Object[] paramArrayOfObject)
    throws StopExecException;

  public abstract void errOnResolveParameter(Session paramSession, Exception paramException, Method paramMethod, Object[] paramArrayOfObject);

  public abstract void errOnExecute(Session paramSession, Exception paramException, Method paramMethod, Object[] paramArrayOfObject);

  public abstract void errOnMessageReceived(Session paramSession, Exception paramException);

  public abstract void errOnCaughtException(Session paramSession, ExceptionEvent paramExceptionEvent);

  public abstract void executeOnChannelClosed(Session paramSession);
}
