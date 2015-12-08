/**
* Filename    : GameServerTest.java
* Author      : Jack
* Create time : 2015-4-14 下午2:31:50
* Description :
*/
package com.cndw.rpg.test;

import com.cndw.rpg.framework.socket.GameServer;
import java.io.PrintStream;

public class GameServerTest
{
  public static void main(String[] args)
  {
    GameServer server = new GameServer(7776);

    server.start();

    System.out.println("start ok");
  }
}
