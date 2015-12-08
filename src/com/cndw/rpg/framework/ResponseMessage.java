/**
* Filename    : ResponseMessage.java
* Author      : Jack
* Create time : 2015-4-14 下午2:32:31
* Description :
*/
package com.cndw.rpg.framework;

import com.cndw.rpg.framework.codec.AmfMessageEncode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jboss.netty.buffer.ChannelBuffer;

public class ResponseMessage
{
  public int r;
  public String module;
  public String action;
  public String requestId;
  public Object m;
  public Object b;
  public long t;
  public static final int typeSuc = 0;
  public static final int typeErr = 1;
  public static final int typeTicpSuc = 2;
  public static final int typeTicpErr = 3;
  public static final int typeTicpChat = 8;
  public static final int typeConfirm = 9;

  public ResponseMessage()
  {
    this.t = System.currentTimeMillis();
  }

  public void setModuleAndAction(String module, String action)
  {
    this.module = module;
    this.action = action;
  }

  public void setMessageHead(String module, String action, String requestId)
  {
    this.module = module;
    this.action = action;
    this.requestId = requestId;
  }

  public ChannelBuffer serialize()
    throws IOException
  {
    return AmfMessageEncode.serialize(this);
  }

  public static ResponseMessage getSuccMessage(Object data)
  {
    return setMessage(data, 0, null);
  }

  public static ResponseMessage getFailMessage(Object data)
  {
    return setMessage(data, 1, null);
  }

  private static ResponseMessage setMessage(Object m, int r, Object b) {
    ResponseMessage resp = new ResponseMessage();
    resp.r = r;
    resp.m = m;
    resp.b = b;
    return resp;
  }

  private static Map<String, Object> setPop(Object message, int type) {
    Map map = new HashMap();
    map.put("message", message);
    map.put("messageType", Integer.valueOf(type));
    return map;
  }

  public static ResponseMessage errMsg(String message)
  {
    return errMsg(message, null);
  }

  public static ResponseMessage errMsg(String message, Object m) {
    Map pop = setPop(message, 1);
    return setMessage(m, 1, pop);
  }

  public static ResponseMessage sucMsg(String message)
  {
    return sucMsg(message, null);
  }

  public static ResponseMessage sucMsg(String message, Object m) {
    Map pop = setPop(message, 0);
    return setMessage(m, 0, pop);
  }

  public static ResponseMessage ticpMsg(String message)
  {
    return ticpMsg(message, null);
  }

  public static ResponseMessage ticpMsg(String message, Object m) {
    Map pop = setPop(message, 2);
    return setMessage(m, 2, pop);
  }

  public static ResponseMessage ticpErrMsg(String message)
  {
    return ticpErrMsg(message, null);
  }

  public static ResponseMessage ticpErrMsg(String message, Object m) {
    Map pop = setPop(message, 3);
    return setMessage(m, 3, pop);
  }

  public static ResponseMessage chatMsg(String message) {
    return chatMsg(message, null);
  }

  public static ResponseMessage chatMsg(String message, Object m) {
    Map pop = setPop(message, 8);
    return setMessage(m, 8, pop);
  }

  public static ResponseMessage confirmMsg(String message, String m, String a, int comfirmType, Object parameters)
  {
    Map pop = setPop(message, 9);
    pop.put("pm", m);
    pop.put("pa", a);
    pop.put("pp", parameters);
    pop.put("pt", Integer.valueOf(comfirmType));
    return setMessage(null, 3, pop);
  }
}
