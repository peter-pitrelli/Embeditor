/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package embeditor.windowmanager;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 *
 * @author thommy
 */
public abstract class WindowManager
{

  private static WindowManager instance;

  public static WindowManager getInstance()
  {
    if (instance == null)
    {
      if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1)
      {
        throw new UnsupportedOperationException("No windowmanager for Windows available");
      }
      else
      {
        if (System.getProperty("os.name").toLowerCase().indexOf("linux") > -1)
        {
          instance = new LinuxWindowManager();
        }
        else
        {
          if (System.getProperty("os.name").toLowerCase().indexOf("mac") > -1)
          {
            throw new UnsupportedOperationException("No windowmanager for MAC OS available");
          }
          else
          {
            throw new UnsupportedOperationException("No windowmanager for Zour OS available");
          }
        }
      }

    }
    return instance;
  }

  public abstract Window findWindow(String name);
  
  public abstract boolean isWindow(Window w);
  
  public abstract Window findByPID(int pid);

  public abstract void moveWindow(Window w, int x, int y);

  public abstract void resizeWindow(Window w, int x, int y);

  public abstract Rectangle getWindowRect(Window w);

  public abstract void setAbove(Window w, boolean above);
  
  public void setWindowRect(Window w, Rectangle r)
  {
    moveWindow(w, r.x, r.y);
    resizeWindow(w, r.width, r.height);
  }
  
  public BufferedImage getScreenshot(Rectangle r)
  {
    try
    {
      Robot robot = new Robot();
      return robot.createScreenCapture(r);
    }
    catch (AWTException e)
    {
      return null;
    }
  }

  public Dimension getDesktopDimension()
  {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    return toolkit.getScreenSize();
  }
  
  public abstract void sendClose(Window w);
}