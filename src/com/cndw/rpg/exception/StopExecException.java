/**
* Filename    : StopExecException.java
* Author      : Jack
* Create time : 2015-4-14 下午2:21:55
* Description :
*/
package com.cndw.rpg.exception;

public class StopExecException extends Exception
{
  private static final long serialVersionUID = -1162119744218004522L;
  private Throwable ex;

  public StopExecException()
  {
    super();
  }

  public StopExecException(String s) {
    super(s, null);
  }

  public StopExecException(String s, Throwable throwable) {
    super(s, null);
    this.ex = throwable;
  }

  public Throwable getException() {
    return this.ex;
  }

  public Throwable getCause() {
    return this.ex;
  }
}
