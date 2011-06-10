/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor;

import embeditor.windowmanager.Window;
import embeditor.windowmanager.WindowManager;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author thommy
 */
public class Embeditor
{

    private WindowManager wm = WindowManager.getInstance();
    
    public void editFile(FileObject result) throws InterruptedException, IOException
    {
        Program p = result.program;
        Process proc = Runtime.getRuntime().exec(new String[]{
          p.command,result.file.getAbsolutePath()  
        });
        //TODO: implement buttons
        Thread.sleep(10000);
        Window w = wm.findWindow("replace");
        wm.setWindowRect(w, p.getOuterRectangle(new Rectangle(result.position.x, result.position.y, result.screenshot.getWidth(), result.screenshot.getHeight())));
        Thread.sleep(10000);
        Rectangle outer = wm.getWindowRect(w);
        result.screenshot = wm.getScreenshot(p.getInnerRectangle(outer));
        result.position = outer.getLocation();
        wm.sendClose(w);
        proc.waitFor();
    }
    
    public FileObject runProgram(Program p, Rectangle r) throws FileNotFoundException, IOException, InterruptedException
    {
        FileObject result = new FileObject();
        result.program = p;
        result.position = r.getLocation();
        result.file = new File("replacemetorandom."+p.fileendig);
        FileInputStream in = new FileInputStream(p.template);
        FileOutputStream out = new FileOutputStream(result.file);
        while(in.available() > 0)
        {
            out.write(in.read());
        }
        out.close();
        in.close();
        Process proc = Runtime.getRuntime().exec(new String[]{
          p.command,result.file.getAbsolutePath()  
        });
        //TODO: implement buttons
        Thread.sleep(10000);
        Window w = wm.findWindow("replace");
        wm.setWindowRect(w, p.getOuterRectangle(r));
        Thread.sleep(10000);
        Rectangle outer = wm.getWindowRect(w);
        result.screenshot = wm.getScreenshot(p.getInnerRectangle(outer));
        result.position = outer.getLocation();
        wm.sendClose(w);
        proc.waitFor();
        return result;
    }
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    // TODO code application logic here
  }
}
