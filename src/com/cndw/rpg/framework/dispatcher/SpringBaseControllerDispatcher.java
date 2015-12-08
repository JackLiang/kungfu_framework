/**
* Filename    : SpringBaseControllerDispatcher.java
* Author      : Jack
* Create time : 2015-4-14 下午2:39:12
* Description :
*/
package com.cndw.rpg.framework.dispatcher;

import com.cndw.rpg.annotation.Controller;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringBaseControllerDispatcher extends ControllerDispatcher
  implements ApplicationContextAware, InitializingBean
{
  private ApplicationContext applicationContext;

  public void afterPropertiesSet()
    throws Exception
  {
    setInstance(this);
  }

  public void setApplicationContext(ApplicationContext arg0) throws BeansException
  {
    this.applicationContext = arg0;
  }

  protected void initController()
  {
    Map handlerMap = this.applicationContext.getBeansWithAnnotation(Controller.class);
    for (Iterator localIterator = handlerMap.values().iterator(); localIterator.hasNext(); ) { Object instance = localIterator.next();
      Class clazz = instance.getClass();
      if (clazz.getPackage().getName().startsWith("com.cndw.kungfu"))
      {
        String className = clazz.getSimpleName();
        Method[] methods = clazz.getMethods();
        for (Method method : methods)
        {
          if (checkParameterTypes(method))
          {
            String methodName = method.getName();
            this.controllerMethodCacheMap.put(
//              getControllerKey(className, methodName), new ControllerDispatcher.MethodHolder(this, instance, method));
            getControllerKey(className, methodName), new ControllerDispatcher.MethodHolder(instance, method));
          }
        }
      }
    }
  }
}
