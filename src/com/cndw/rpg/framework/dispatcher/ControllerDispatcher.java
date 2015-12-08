/**
* Filename    : ControllerDispatcher.java
* Author      : Jack
* Create time : 2015-4-14 下午2:38:45
* Description :
*/
package com.cndw.rpg.framework.dispatcher;

import com.cndw.rpg.annotation.UnAuth;
import com.cndw.rpg.exception.StopExecException;
import com.cndw.rpg.framework.ResponseMessage;
import com.cndw.rpg.framework.Session;
import com.cndw.rpg.framework.business.RequestHandler;
import com.cndw.rpg.framework.codec.AmfMessageDecode;
import com.cndw.rpg.framework.codec.AmfMessageEncode;
import com.cndw.rpg.framework.dispatcher.ControllerDispatcher.MethodHolder;

import flex.messaging.io.amf.Amf3Input;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

public abstract class ControllerDispatcher
{
  protected Map<String, MethodHolder> controllerMethodCacheMap = new HashMap<String, MethodHolder>();

private Object[] params;

  protected static final Log log = LogFactory.getLog(ControllerDispatcher.class);
  protected static final String BASEPACKET = "com.cndw.kungfu";
  protected static volatile ControllerDispatcher dispatcher;
  private static AtomicBoolean hasInit = new AtomicBoolean(false);

  public static ControllerDispatcher getInstance() {
    if (dispatcher == null) {
      synchronized (ControllerDispatcher.class) {
        if (dispatcher == null) {
          dispatcher = new ClasspathControllerDispatcher();
        }
      }
    }
    dispatcher.checkInit();
    return dispatcher;
  }

  public static boolean setInstance(ControllerDispatcher instance)
  {
    if (dispatcher == null) {
      synchronized (ControllerDispatcher.class) {
        if (dispatcher == null) {
          dispatcher = instance;
          return true;
        }
      }
    }
    return false;
  }

  /**调度封装好的amfInput
 * @param requestHandler
 * @param session
 * @param amfInput
 * @throws ClassNotFoundException
 * @throws IOException
 * @throws StopExecException
 */
public void dispatch(RequestHandler requestHandler, Session session, Amf3Input amfInput) throws ClassNotFoundException, IOException, StopExecException {
    String controllerName = amfInput.readObject().toString();
    String controllerMethod = amfInput.readObject().toString();
    String requestId = amfInput.readObject().toString();
    if ((controllerName == null) || (controllerMethod == null) || (requestId == null)) {
      log.warn("WARN|no input controller for " + controllerName + ":" + controllerMethod + ":" + requestId);
      return;
    }
    MethodHolder holder = (MethodHolder)this.controllerMethodCacheMap.get(getControllerKey(controllerName, controllerMethod));
    if (holder == null) {
      log.warn("WARN|no controller for " + controllerName + ":" + controllerMethod + " | " + session.getAccountName());
      return;
    }

    Annotation unAuth = holder.method.getAnnotation(UnAuth.class);
    if ((unAuth == null) && (!session.isLogin())) {
      return;
    }
    try
    {
      params = AmfMessageDecode.deSerialize(holder.method.getParameterTypes(), amfInput);
    }
    catch (Exception e)
    {
      Object[] params;
      requestHandler.errOnResolveParameter(session, e, holder.method, null);
      return;
    }
//    Object[] params = null;
    requestHandler.beforeExecuteByUnpack(session, holder.method, params);

    params[0] = session;
    try
    {
      Object result = holder.invoke(params);
      if ((result != null) && 
        (session.getChannel().isConnected())) {
        if ((result instanceof ResponseMessage)) {
          ((ResponseMessage)result).setMessageHead(controllerName, controllerMethod, requestId);
          session.getChannel().write(AmfMessageEncode.serialize(result));
        } else {
          ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
          buffer.writeBytes(result.toString().getBytes());
          session.getChannel().write(buffer);
        }
      }

    }
    catch (Exception e)
    {
      requestHandler.errOnExecute(session, e, holder.method, params);
    }
  }

  public void dispatchExt(Channel channel, String className, String methodName, Object[] params)
  {
    MethodHolder holder = (MethodHolder)this.controllerMethodCacheMap.get(getControllerKey(className, methodName));
    if (holder != null)
      try {
        holder.invoke(params);
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

  protected void checkInit()
  {
    if (!hasInit.get())
      synchronized (ControllerDispatcher.class) {
        if (!hasInit.get()) {
          dispatcher.initController();
          hasInit.set(true);
        }
      }
  }

  protected String getControllerKey(String className, String methodName)
  {
    return className + ":" + methodName;
  }

  protected abstract void initController();

  protected boolean checkParameterTypes(Method method)
  {
    Class[] paramTypes = method.getParameterTypes();
    if ((paramTypes == null) || (paramTypes.length < 1)) {
      return false;
    }
    return Session.class.isAssignableFrom(paramTypes[0]);
  }
  protected class MethodHolder {
    private Object owner;
    private Method method;

    public MethodHolder(Object clazz, Method method) {
      this.owner = clazz;
      this.method = method;
    }

    public Object invoke(Object[] params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      return this.method.invoke(this.owner, params);
    }
  }
}
