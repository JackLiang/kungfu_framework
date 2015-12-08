/**
* Filename    : ClasspathControllerDispatcher.java
* Author      : Jack
* Create time : 2015-4-14 下午2:38:18
* Description :
*/
package com.cndw.rpg.framework.dispatcher;

import com.cndw.rpg.annotation.Controller;
import com.cndw.rpg.base.ClassScan;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class ClasspathControllerDispatcher extends ControllerDispatcher
{
  protected ClasspathControllerDispatcher()
  {
    setInstance(this);
  }

  protected void initController()
  {
    ClassScan classScan = new ClassScan("com.cndw.kungfu");
    Set<Class<?>> classSet = classScan.getClassesByAnnotation(Controller.class);
    if (classSet == null) return;
    for (Class<?> clazz : classSet)
    	
      try
      {
        Object instance = clazz.newInstance();
        String className = clazz.getSimpleName();
        Method[] methods = clazz.getMethods();
        if (methods != null)
          for (Method method : methods)
            if (checkParameterTypes(method))
            {
              String methodName = method.getName();
							this.controllerMethodCacheMap.put(
                getControllerKey(className, methodName), new ControllerDispatcher.MethodHolder(instance, method));
//              getControllerKey(className, methodName), new ControllerDispatcher.MethodHolder(this, instance, method));
            }
      }
      catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
  }

  public Object testDispatch(String controllerName, String controllerMethod, Object[] params)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
  {
    checkInit();
    ControllerDispatcher.MethodHolder holder = (ControllerDispatcher.MethodHolder)this.controllerMethodCacheMap.get(getControllerKey(controllerName, controllerMethod));
    return holder.invoke(params);
  }
}
