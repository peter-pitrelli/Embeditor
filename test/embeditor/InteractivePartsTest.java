/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor;

import java.io.IOException;
import javax.swing.JOptionPane;


/**
 *
 * @author thommy
 */
public class InteractivePartsTest
{
  
  public void main(String[] args) throws IOException{
    String progname = JOptionPane.showInputDialog(null, "Bite geben Sie einen Programmbefehl ein");
    Process p = Runtime.getRuntime().exec(progname);
    p.
    
  }

}
