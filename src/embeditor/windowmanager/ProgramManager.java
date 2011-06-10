/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor.windowmanager;

import java.io.IOException;

/**
 *
 * @author thommy
 */
public class ProgramManager
{

  /**
   * Runs the command with Runtime.exec
   * and returns an Array which contains
   * the PID as Integer and the Process
   * Object (ugly ugly... bad java programmer)
   * @param command
   * @param p
   * @return 
   */
  Object[] getPid(String[] command) throws IOException
  {
    byte[] bo = new byte[100];
    String[] cmd = new String[command.length+3];
    cmd[0] = "bash";
    cmd[1] = "-c";
    int i;
    for (i=2;i<2+command.length;i++)
    {
      cmd[i]=command[i-2];
    };
    cmd[i]="&";
    Process p = Runtime.getRuntime().exec(cmd);
    p.getInputStream().read(bo);
    System.out.println(new String(bo));
    return new Object[String.]
  }
}
