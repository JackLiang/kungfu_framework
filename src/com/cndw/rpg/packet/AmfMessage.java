/**
* Filename    : AmfMessage.java
* Author      : Jack
* Create time : 2015-4-14 下午2:30:04
* Description :
*/
package com.cndw.rpg.packet;

import java.io.Serializable;

public class AmfMessage
  implements Serializable
{
  private static final long serialVersionUID = -2914816402149656728L;
  public String controllerName;
  public String controllerMethod;
}